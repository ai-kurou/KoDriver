package kurou.kodriver.feature.lmuwindowsnarrator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.usecase.DetermineLmuWindowsNarratorReadoutUseCase
import kurou.kodriver.domain.usecase.LmuWindowsNarratorReadoutSettings
import kurou.kodriver.domain.usecase.LmuWindowsNarratorState
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
    private val determineReadout: DetermineLmuWindowsNarratorReadoutUseCase,
    private val currentTimeMs: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {

    private var narratorState = LmuWindowsNarratorState()

    private val selectedSimulator = readoutListUseCases.observeSelectedSimulator()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val enabledStates = combine(
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator == null) emptyFlow() else readoutListUseCases.observeReadoutEnabledStates(simulator.id)
            },
        flagUseCases.observeFlagEnabledStates(),
        vehicleDamageUseCases.observeVehicleDamageEnabledStates(),
    ) { readoutStates, flagStates, vehicleDamageStates -> readoutStates + flagStates + vehicleDamageStates }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    // index が小さいほど優先度が高い（リスト上位 = 高優先）
    private val readoutOrder = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator == null) emptyFlow() else readoutListUseCases.observeReadoutOrder(simulator.id)
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
            if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
            vehicleApproachUseCases.observeProximity()
        }
        .onEach { proximity ->
            val decision = determineReadout.determineVehicleApproach(
                state = narratorState,
                proximity = proximity,
                settings = currentSettings,
                observedAtMs = currentTimeMs(),
            )
            narratorState = decision.state
            decision.events.forEach(::speakWithPriority)
        }
        .launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val overheatingJob = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
            vehicleDamageUseCases.observeVehicleDamage()
        }
        .onEach { vehicleDamage ->
            val decision = determineReadout.determineVehicleDamage(
                state = narratorState,
                vehicleDamage = vehicleDamage,
                settings = currentSettings,
            )
            narratorState = decision.state
            decision.events.forEach(::speakWithPriority)
        }
        .launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val flagJob = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
            flagUseCases.observeRaceFlags()
        }
        .onEach { raceFlags ->
            val decision = determineReadout.determineRaceFlags(
                state = narratorState,
                raceFlags = raceFlags,
                settings = currentSettings,
            )
            narratorState = decision.state
            decision.events.forEach(::speakWithPriority)
        }
        .launchIn(viewModelScope)

    private val currentSettings: LmuWindowsNarratorReadoutSettings
        get() = LmuWindowsNarratorReadoutSettings(
            enabledStates = enabledStates.value,
            currentLap = currentLap.value,
            skipFirstLap = skipFirstLap.value,
            vehicleApproachStartReadoutEnabled = startReadoutEnabled.value,
            vehicleApproachStartReadoutType = startReadoutType.value,
        )

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
