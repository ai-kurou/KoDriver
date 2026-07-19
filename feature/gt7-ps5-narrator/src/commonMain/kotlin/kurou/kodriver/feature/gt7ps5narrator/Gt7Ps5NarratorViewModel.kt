package kurou.kodriver.feature.gt7ps5narrator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.DetermineGt7Ps5NarratorReadoutUseCase
import kurou.kodriver.domain.usecase.Gt7Ps5NarratorReadoutSettings
import kurou.kodriver.domain.usecase.Gt7Ps5NarratorState
import kurou.kodriver.domain.usecase.ObserveGt7Ps5MyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

internal data class MyBestLapUseCases(
    val observeGt7Ps5: ObserveGt7Ps5UseCase,
    val observeMyBestLapVoiceType: ObserveGt7Ps5MyBestLapVoiceTypeUseCase,
)

internal data class ReadoutListUseCases(
    val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    val observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    val observeReadoutOrder: ObserveReadoutOrderUseCase,
    val observeQueueEnabledStates: ObserveQueueEnabledStatesUseCase,
)

internal data class RemainingFuelLapsUseCases(
    val observeRemainingFuelLapsThreshold: ObserveGt7Ps5RemainingFuelLapsUseCase,
)

@OptIn(ExperimentalCoroutinesApi::class)
internal class Gt7Ps5NarratorViewModel(
    myBestLapUseCases: MyBestLapUseCases,
    readoutListUseCases: ReadoutListUseCases,
    remainingFuelLapsUseCases: RemainingFuelLapsUseCases,
    private val eventProcessor: Gt7Ps5NarratorEventProcessor,
    private val determineGt7Ps5NarratorReadout: DetermineGt7Ps5NarratorReadoutUseCase =
        DetermineGt7Ps5NarratorReadoutUseCase(),
    private val currentTimeMs: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {

    private val selectedSimulator = readoutListUseCases.observeSelectedSimulator()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // GT7にはdetailPaneのサブトグルが存在しないため、listPaneの状態のみで完結する。
    private val listEnabledStates = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator == null) emptyFlow() else readoutListUseCases.observeReadoutEnabledStates(simulator.id)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val readoutOrder = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator == null) emptyFlow() else readoutListUseCases.observeReadoutOrder(simulator.id)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // キューに追加して読み上げるかどうか（ReadoutItemKey.TopLevel 単位）。
    private val queueEnabledStates = readoutListUseCases.observeQueueEnabledStates()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap<ReadoutItemKey, Boolean>())

    private val voiceType = myBestLapUseCases.observeMyBestLapVoiceType()
        .stateIn(viewModelScope, SharingStarted.Eagerly, MyBestLapVoiceType.FORMAL)

    private val fuelThreshold = remainingFuelLapsUseCases.observeRemainingFuelLapsThreshold()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 3)

    private var narratorState = Gt7Ps5NarratorState()

    private val gt7TelemetryFlow = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator !is Simulator.Gt7Ps5) emptyFlow()
            else myBestLapUseCases.observeGt7Ps5()
        }
        .shareIn(viewModelScope, SharingStarted.Eagerly)

    private val currentSettings: Gt7Ps5NarratorReadoutSettings
        get() = Gt7Ps5NarratorReadoutSettings(
            enabledStates = listEnabledStates.value,
            myBestLapVoiceType = voiceType.value,
            remainingFuelLapsThreshold = fuelThreshold.value,
            remainingFuelLapsEnabled = listEnabledStates.value.getValue(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root),
        )

    @Suppress("UnusedPrivateProperty")
    private val myBestLapJob = gt7TelemetryFlow
        .onEach { telemetry ->
            val observedAtMs = currentTimeMs()
            val state = narratorState
            val settings = currentSettings
            val decision = determineGt7Ps5NarratorReadout.determineMyBestLap(
                state = state,
                telemetry = telemetry,
                settings = settings,
            )
            narratorState = decision.state
            eventProcessor.process(
                sourceKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root,
                telemetry = telemetry,
                events = decision.events,
                readoutOrder = readoutOrder.value,
                queueEnabledStates = queueEnabledStates.value,
                observedAtMs = observedAtMs,
                logContext = Gt7Ps5TelemetryLogContext(
                    state = state,
                    settings = settings,
                    finalState = decision.state,
                ),
            )
        }
        .launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val remainingFuelLapsJob = gt7TelemetryFlow
        .onEach { telemetry ->
            val observedAtMs = currentTimeMs()
            val state = narratorState
            val settings = currentSettings
            val decision = determineGt7Ps5NarratorReadout.determineRemainingFuelLaps(
                state = state,
                telemetry = telemetry,
                settings = settings,
                observedAtMs = observedAtMs,
            )
            narratorState = decision.state
            eventProcessor.process(
                sourceKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root,
                telemetry = telemetry,
                events = decision.events,
                readoutOrder = readoutOrder.value,
                queueEnabledStates = queueEnabledStates.value,
                observedAtMs = observedAtMs,
                logContext = Gt7Ps5TelemetryLogContext(
                    state = state,
                    settings = settings,
                    finalState = decision.state,
                ),
            )
        }
        .launchIn(viewModelScope)
}
