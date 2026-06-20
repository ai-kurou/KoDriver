package kurou.kodriver.feature.lmuwindowsnarrator

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
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.model.VehicleDamageData
import kurou.kodriver.domain.usecase.ObserveFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveProximityUseCase
import kurou.kodriver.domain.usecase.ObserveRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveVehicleApproachSkipFirstLapUseCase
import kurou.kodriver.domain.usecase.ObserveVehicleApproachStartReadoutEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveVehicleApproachStartReadoutTypeUseCase
import kurou.kodriver.domain.usecase.ObserveVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveVehicleDamageUseCase

data class VehicleApproachUseCases(
    val observeProximity: ObserveProximityUseCase,
    val observeLmuWindows: ObserveLmuWindowsUseCase,
    val observeSkipFirstLap: ObserveVehicleApproachSkipFirstLapUseCase,
    val observeStartReadoutEnabled: ObserveVehicleApproachStartReadoutEnabledUseCase,
    val observeStartReadoutType: ObserveVehicleApproachStartReadoutTypeUseCase,
)

data class VehicleDamageUseCases(
    val observeVehicleDamage: ObserveVehicleDamageUseCase,
    val observeVehicleDamageEnabledStates: ObserveVehicleDamageEnabledStatesUseCase,
)

data class ReadoutListUseCases(
    val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    val observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    val observeReadoutOrder: ObserveReadoutOrderUseCase,
)

data class FlagUseCases(
    val observeRaceFlags: ObserveRaceFlagsUseCase,
    val observeFlagEnabledStates: ObserveFlagEnabledStatesUseCase,
)

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsNarratorViewModel(
    vehicleApproachUseCases: VehicleApproachUseCases,
    vehicleDamageUseCases: VehicleDamageUseCases,
    readoutListUseCases: ReadoutListUseCases,
    flagUseCases: FlagUseCases,
    private val ttsEngine: TextToSpeechEngine,
    private val currentTimeMs: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {

    private val selectedSimulator = readoutListUseCases.observeSelectedSimulator()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val enabledStates = combine(
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator == null) emptyFlow() else readoutListUseCases.observeReadoutEnabledStates(simulator)
            },
        flagUseCases.observeFlagEnabledStates(),
        vehicleDamageUseCases.observeVehicleDamageEnabledStates(),
    ) { readoutStates, flagStates, vehicleDamageStates -> readoutStates + flagStates + vehicleDamageStates }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    // index が小さいほど優先度が高い（リスト上位 = 高優先）
    private val readoutOrder = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator == null) emptyFlow() else readoutListUseCases.observeReadoutOrder(simulator)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val currentLap = vehicleApproachUseCases.observeLmuWindows()
        .map { it.timing.currentLap }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val skipFirstLap = vehicleApproachUseCases.observeSkipFirstLap()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val startReadoutEnabled = vehicleApproachUseCases.observeStartReadoutEnabled()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val startReadoutType = vehicleApproachUseCases.observeStartReadoutType()
        .stateIn(viewModelScope, SharingStarted.Eagerly, VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)

    @Suppress("UnusedPrivateProperty")
    private val proximityJob = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator != LMU_WINDOWS_SIMULATOR_KEY) return@flatMapLatest emptyFlow()
            val initialScan =
                ApproachTimestamps(emptyMap(), emptyMap()) to (VehicleApproachEvent.None as VehicleApproachEvent)
            vehicleApproachUseCases.observeProximity()
                .scan(initialScan) { (timestamps, _), current ->
                    val now = currentTimeMs()
                    var leftAnnounce = false
                    var rightAnnounce = false

                    val newLeft = current.sideBySideLeftVehicleIds.associateWith { id ->
                        val prev = timestamps.left[id]
                        if (prev == null) {
                            ApproachState(startedAtMs = now, announced = false)
                        } else {
                            val shouldAnnounce = !prev.announced && now - prev.startedAtMs >= APPROACH_DEBOUNCE_MS
                            if (shouldAnnounce) leftAnnounce = true
                            prev.copy(announced = prev.announced || shouldAnnounce)
                        }
                    }
                    val newRight = current.sideBySideRightVehicleIds.associateWith { id ->
                        val prev = timestamps.right[id]
                        if (prev == null) {
                            ApproachState(startedAtMs = now, announced = false)
                        } else {
                            val shouldAnnounce = !prev.announced && now - prev.startedAtMs >= APPROACH_DEBOUNCE_MS
                            if (shouldAnnounce) rightAnnounce = true
                            prev.copy(announced = prev.announced || shouldAnnounce)
                        }
                    }

                    val event = when {
                        leftAnnounce && rightAnnounce -> VehicleApproachEvent.Simultaneous
                        leftAnnounce -> VehicleApproachEvent.Single(ApproachSide.LEFT)
                        rightAnnounce -> VehicleApproachEvent.Single(ApproachSide.RIGHT)
                        else -> VehicleApproachEvent.None
                    }
                    ApproachTimestamps(newLeft, newRight) to event
                }
        }
        .onEach { (_, event) ->
            if (enabledStates.value[ReadoutItemKey.VEHICLE_APPROACH] == false) return@onEach
            if (!startReadoutEnabled.value) return@onEach
            // mLapNumber は 0 スタート（最初の計測周 = 0、フォーメーションラップは負値の可能性あり）
            if (skipFirstLap.value && currentLap.value <= 0) return@onEach

            when (event) {
                is VehicleApproachEvent.Single -> {
                    speakWithPriority(event.side.toSpeechEvent(startReadoutType.value))
                }
                VehicleApproachEvent.None,
                VehicleApproachEvent.Simultaneous,
                -> Unit
            }
        }
        .launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val overheatingJob = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator != LMU_WINDOWS_SIMULATOR_KEY) return@flatMapLatest emptyFlow()
            vehicleDamageUseCases.observeVehicleDamage()
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
            if (simulator != LMU_WINDOWS_SIMULATOR_KEY) return@flatMapLatest emptyFlow()
            flagUseCases.observeRaceFlags()
                .scan(null as RaceFlagsData? to null as RaceFlagsData?) { acc, current ->
                    acc.second to current
                }
                .drop(1)
        }
        .onEach { (prev, current) ->
            if (prev == null || current == null) return@onEach
            announceFlags(prev, current)
        }
        .launchIn(viewModelScope)

    private fun announceFlags(prev: RaceFlagsData, current: RaceFlagsData) {
        if (enabledStates.value[ReadoutItemKey.BLUE_FLAG] != false) {
            if (prev.playerFlag != PrimaryFlag.BLUE && current.playerFlag == PrimaryFlag.BLUE) {
                speakWithPriority(SpeechEvent.BlueFlag)
            }
        }

        if (enabledStates.value[ReadoutItemKey.SECTOR_YELLOW_FLAG] != false) {
            val newYellowSector = current.sectorFlags.indices.any { i ->
                current.sectorFlags[i] == SectorFlagState.YELLOW &&
                    prev.sectorFlags.getOrNull(i) != SectorFlagState.YELLOW
            }
            if (newYellowSector) {
                speakWithPriority(SpeechEvent.YellowFlag)
            }
        }

        if (enabledStates.value[ReadoutItemKey.FULL_COURSE_YELLOW] != false) {
            if (prev.gamePhase != SessionPhase.FULL_COURSE_YELLOW &&
                current.gamePhase == SessionPhase.FULL_COURSE_YELLOW
            ) {
                speakWithPriority(SpeechEvent.FullCourseYellow)
            }
        }

        if (enabledStates.value[ReadoutItemKey.RED_FLAG] != false) {
            if (prev.gamePhase != SessionPhase.RED_FLAG &&
                current.gamePhase == SessionPhase.RED_FLAG
            ) {
                speakWithPriority(SpeechEvent.SessionStop)
            }
        }
    }

    private companion object {
        const val LMU_WINDOWS_SIMULATOR_KEY = "lmu_windows"
        const val APPROACH_DEBOUNCE_MS = 50L
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

private enum class ApproachSide {
    LEFT,
    RIGHT,
    ;

    fun toSpeechEvent(readoutType: VehicleApproachStartReadoutType): SpeechEvent =
        when (this) {
            LEFT -> when (readoutType) {
                VehicleApproachStartReadoutType.CAR_LEFT_RIGHT -> SpeechEvent.CarLeft
                VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH -> SpeechEvent.LeftApproach
            }
            RIGHT -> when (readoutType) {
                VehicleApproachStartReadoutType.CAR_LEFT_RIGHT -> SpeechEvent.CarRight
                VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH -> SpeechEvent.RightApproach
            }
        }
}

private data class ApproachState(
    val startedAtMs: Long,
    val announced: Boolean,
)

private data class ApproachTimestamps(
    val left: Map<Int, ApproachState>,
    val right: Map<Int, ApproachState>,
)

private sealed interface VehicleApproachEvent {
    data object None : VehicleApproachEvent
    data object Simultaneous : VehicleApproachEvent
    data class Single(val side: ApproachSide) : VehicleApproachEvent
}
