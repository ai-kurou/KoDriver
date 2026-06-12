package kurou.kodriver.feature.narrator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.VehicleDamageData
import kurou.kodriver.domain.usecase.ObserveFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuUseCase
import kurou.kodriver.domain.usecase.ObserveProximityUseCase
import kurou.kodriver.domain.usecase.ObserveRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveSkipFirstLapUseCase
import kurou.kodriver.domain.usecase.ObserveVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveVehicleDamageUseCase

data class VehicleApproachUseCases(
    val observeProximity: ObserveProximityUseCase,
    val observeLmu: ObserveLmuUseCase,
    val observeSkipFirstLap: ObserveSkipFirstLapUseCase,
    val observeVehicleDamage: ObserveVehicleDamageUseCase,
)

@OptIn(ExperimentalCoroutinesApi::class)
class LmuNarratorViewModel(
    vehicleApproachUseCases: VehicleApproachUseCases,
    observeRaceFlagsUseCase: ObserveRaceFlagsUseCase,
    observeSelectedSimulatorUseCase: ObserveSelectedSimulatorUseCase,
    observeReadoutEnabledStatesUseCase: ObserveReadoutEnabledStatesUseCase,
    observeFlagEnabledStatesUseCase: ObserveFlagEnabledStatesUseCase,
    observeVehicleDamageEnabledStatesUseCase: ObserveVehicleDamageEnabledStatesUseCase,
    observeReadoutOrderUseCase: ObserveReadoutOrderUseCase,
    private val ttsEngine: TextToSpeechEngine,
) : ViewModel() {

    private val selectedSimulator = observeSelectedSimulatorUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val enabledStates = combine(
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator == null) emptyFlow() else observeReadoutEnabledStatesUseCase(simulator)
            },
        observeFlagEnabledStatesUseCase(),
        observeVehicleDamageEnabledStatesUseCase(),
    ) { readoutStates, flagStates, vehicleDamageStates -> readoutStates + flagStates + vehicleDamageStates }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    // index が小さいほど優先度が高い（リスト上位 = 高優先）
    private val readoutOrder = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator == null) emptyFlow() else observeReadoutOrderUseCase(simulator)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val currentLap = vehicleApproachUseCases.observeLmu()
        .map { it.timing.currentLap }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val skipFirstLap = vehicleApproachUseCases.observeSkipFirstLap()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    @Suppress("UnusedPrivateProperty")
    private val proximityJob = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator != LMU_SIMULATOR_KEY) return@flatMapLatest emptyFlow()
            vehicleApproachUseCases.observeProximity()
                .scan(null as ProximityData? to null as ProximityData?) { acc, current ->
                    acc.second to current
                }
                .drop(1)
        }
        .onEach { (prev, current) ->
            if (current == null) return@onEach
            if (enabledStates.value[ReadoutItemKey.VEHICLE_APPROACH] == false) return@onEach
            if (skipFirstLap.value && currentLap.value == 1) return@onEach

            val prevLeftIds = prev?.sideBySideLeftVehicleIds ?: emptySet()
            val prevRightIds = prev?.sideBySideRightVehicleIds ?: emptySet()
            val newLeftVehicle = (current.sideBySideLeftVehicleIds - prevLeftIds).isNotEmpty()
            val newRightVehicle = (current.sideBySideRightVehicleIds - prevRightIds).isNotEmpty()

            when {
                // 両方同時の場合は読み上げないで一旦様子見る
                /*
                newLeftVehicle && newRightVehicle -> {
                    speakWithPriority(SpeechEvent.CarLeft)
                    speakWithPriority(SpeechEvent.CarRight)
                }
                 */
                newLeftVehicle -> speakWithPriority(SpeechEvent.CarLeft)
                newRightVehicle -> speakWithPriority(SpeechEvent.CarRight)
            }
        }
        .launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val overheatingJob = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator != LMU_SIMULATOR_KEY) return@flatMapLatest emptyFlow()
            vehicleApproachUseCases.observeVehicleDamage()
                .scan(null as VehicleDamageData? to null as VehicleDamageData?) { acc, current ->
                    acc.second to current
                }
                .drop(1)
        }
        .onEach { (prev, current) ->
            if (current == null) return@onEach
            if (enabledStates.value[ReadoutItemKey.OVERHEAT] == false) return@onEach
            if (prev?.overheating != true && current.overheating) {
                speakWithPriority(SpeechEvent.Overheating)
            }
        }
        .launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val flagJob = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator != LMU_SIMULATOR_KEY) return@flatMapLatest emptyFlow()
            observeRaceFlagsUseCase()
                .scan(null as RaceFlagsData? to null as RaceFlagsData?) { acc, current ->
                    acc.second to current
                }
                .drop(1)
        }
        .onEach { (prev, current) ->
            if (current == null) return@onEach
            announceFlags(prev, current)
        }
        .launchIn(viewModelScope)

    private fun announceFlags(prev: RaceFlagsData?, current: RaceFlagsData) {
        if (enabledStates.value[ReadoutItemKey.BLUE_FLAG] != false) {
            if (prev?.playerFlag != PrimaryFlag.BLUE && current.playerFlag == PrimaryFlag.BLUE) {
                speakWithPriority(SpeechEvent.BlueFlag)
            }
        }

        if (enabledStates.value[ReadoutItemKey.SECTOR_YELLOW_FLAG] != false) {
            val prevSectors = prev?.sectorFlags ?: emptyList()
            val newYellowSector = current.sectorFlags.indices.any { i ->
                current.sectorFlags[i] == SectorFlagState.YELLOW &&
                    prevSectors.getOrNull(i) != SectorFlagState.YELLOW
            }
            if (newYellowSector) {
                speakWithPriority(SpeechEvent.YellowFlag)
            }
        }

        if (enabledStates.value[ReadoutItemKey.FULL_COURSE_YELLOW] != false) {
            if (prev?.gamePhase != SessionPhase.FULL_COURSE_YELLOW &&
                current.gamePhase == SessionPhase.FULL_COURSE_YELLOW
            ) {
                speakWithPriority(SpeechEvent.FullCourseYellow)
            }
        }

        if (enabledStates.value[ReadoutItemKey.RED_FLAG] != false) {
            if (prev?.gamePhase != SessionPhase.RED_FLAG &&
                current.gamePhase == SessionPhase.RED_FLAG
            ) {
                speakWithPriority(SpeechEvent.SessionStop)
            }
        }
    }

    private companion object {
        const val LMU_SIMULATOR_KEY = "lmu"
    }

    /**
     * 優先度を考慮して読み上げる。
     * - 再生中のアイテムより優先度が高い（order の index が小さい）場合: 現在の再生を停止して割り込む
     * - 再生中のアイテムと同じか優先度が低い場合: 無視する
     */
    private fun speakWithPriority(event: SpeechEvent) {
        val order = readoutOrder.value
        val currentKey = ttsEngine.currentReadoutItemKey
        if (currentKey != null) {
            val currentIndex = order.indexOf(currentKey).takeIf { it != -1 } ?: Int.MAX_VALUE
            val newIndex = order.indexOf(event.readoutItemKey).takeIf { it != -1 } ?: Int.MAX_VALUE
            if (newIndex >= currentIndex) return
            ttsEngine.stop()
        }
        ttsEngine.speak(event)
    }
}
