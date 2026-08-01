package kurou.kodriver.feature.acewindowsnarrator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_DEFAULT_THRESHOLD_PERCENTAGE
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.AceWindowsNarratorReadoutSettings
import kurou.kodriver.domain.usecase.AceWindowsNarratorState
import kurou.kodriver.domain.usecase.DetermineAceWindowsNarratorReadoutUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal data class RemainingFuelUseCases(
    val observeAceWindowsFuel: ObserveAceWindowsFuelUseCase,
    val observeThresholdPercentage: ObserveAceWindowsRemainingFuelThresholdPercentageUseCase,
)

internal data class FlagUseCases(
    val observeAceWindowsFlag: ObserveAceWindowsFlagUseCase,
    val observeFlagEnabledStates: ObserveAceWindowsFlagEnabledStatesUseCase,
)

internal data class ReadoutListUseCases(
    val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    val observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    val observeReadoutOrder: ObserveReadoutOrderUseCase,
    val observeQueueEnabledStates: ObserveQueueEnabledStatesUseCase,
)

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
internal class AceWindowsNarratorViewModel(
    remainingFuelUseCases: RemainingFuelUseCases,
    readoutListUseCases: ReadoutListUseCases,
    flagUseCases: FlagUseCases,
    private val eventProcessor: AceWindowsNarratorEventProcessor,
    private val determineAceWindowsNarratorReadout: DetermineAceWindowsNarratorReadoutUseCase =
        DetermineAceWindowsNarratorReadoutUseCase(),
    private val currentTimeMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : ViewModel() {
    private val selectedSimulator =
        readoutListUseCases
            .observeSelectedSimulator()
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val listEnabledStates =
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator == null) emptyFlow() else readoutListUseCases.observeReadoutEnabledStates(simulator.id)
            }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    // listPane（listEnabledStates）とdetailPane（flagEnabledStates）を統合した、
    // Narratorの読み上げ判定に実際に使う唯一のenabledStates。
    private val mergedEnabledStates =
        combine(
            listEnabledStates,
            flagUseCases.observeFlagEnabledStates(),
        ) { readoutStates, flagStates -> readoutStates + flagStates }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap<ReadoutItemKey, Boolean>())

    private val readoutOrder =
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator == null) emptyFlow() else readoutListUseCases.observeReadoutOrder(simulator.id)
            }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // キューに追加して読み上げるかどうか（ReadoutItemKey.TopLevel 単位）。
    private val queueEnabledStates =
        readoutListUseCases
            .observeQueueEnabledStates()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap<ReadoutItemKey, Boolean>())

    private val remainingFuelThreshold =
        remainingFuelUseCases
            .observeThresholdPercentage()
            .stateIn(viewModelScope, SharingStarted.Eagerly, ACE_WINDOWS_REMAINING_FUEL_DEFAULT_THRESHOLD_PERCENTAGE)

    private var narratorState = AceWindowsNarratorState()

    private val currentSettings: AceWindowsNarratorReadoutSettings
        get() =
            AceWindowsNarratorReadoutSettings(
                enabledStates = mergedEnabledStates.value,
                remainingFuelThresholdPercentage = remainingFuelThreshold.value,
            )

    private val fuelFlow =
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator !is Simulator.AceWindows) emptyFlow() else remainingFuelUseCases.observeAceWindowsFuel()
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    private val flagFlow =
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator !is Simulator.AceWindows) emptyFlow() else flagUseCases.observeAceWindowsFlag()
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    @Suppress("UnusedPrivateProperty")
    private val readoutJob =
        fuelFlow
            .onEach { fuel ->
                val observedAtMs = currentTimeMs()
                val state = narratorState
                val settings = currentSettings
                val decision =
                    determineAceWindowsNarratorReadout.determineRemainingFuel(
                        state = state,
                        data = fuel,
                        settings = settings,
                    )
                narratorState = decision.state
                eventProcessor.processRemainingFuel(
                    fuel = fuel,
                    events = decision.events,
                    readoutOrder = readoutOrder.value,
                    queueEnabledStates = queueEnabledStates.value,
                    observedAtMs = observedAtMs,
                    logContext =
                        AceWindowsTelemetryLogContext(
                            state = state,
                            settings = settings,
                            finalState = decision.state,
                        ),
                )
            }.launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val flagJob =
        flagFlow
            .onEach { flag ->
                val observedAtMs = currentTimeMs()
                val state = narratorState
                val settings = currentSettings
                val decision =
                    determineAceWindowsNarratorReadout.determineFlag(
                        state = state,
                        data = flag,
                        settings = settings,
                    )
                narratorState = decision.state
                eventProcessor.processFlag(
                    flag = flag,
                    events = decision.events,
                    readoutOrder = readoutOrder.value,
                    queueEnabledStates = queueEnabledStates.value,
                    observedAtMs = observedAtMs,
                    logContext =
                        AceWindowsTelemetryLogContext(
                            state = state,
                            settings = settings,
                            finalState = decision.state,
                        ),
                )
            }.launchIn(viewModelScope)
}
