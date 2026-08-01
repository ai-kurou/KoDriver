package kurou.kodriver.feature.gt7ps5narrator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.DetermineGt7Ps5NarratorReadoutUseCase
import kurou.kodriver.domain.usecase.Gt7Ps5NarratorReadoutSettings
import kurou.kodriver.domain.usecase.Gt7Ps5NarratorState
import kurou.kodriver.domain.usecase.ObserveGt7Ps5MyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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

internal data class RemainingFuelUseCases(
    val observeRemainingFuelThresholdPercentage: ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase,
)

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
internal class Gt7Ps5NarratorViewModel(
    myBestLapUseCases: MyBestLapUseCases,
    readoutListUseCases: ReadoutListUseCases,
    remainingFuelLapsUseCases: RemainingFuelLapsUseCases,
    remainingFuelUseCases: RemainingFuelUseCases,
    private val eventProcessor: Gt7Ps5NarratorEventProcessor,
    private val determineGt7Ps5NarratorReadout: DetermineGt7Ps5NarratorReadoutUseCase =
        DetermineGt7Ps5NarratorReadoutUseCase(),
    private val currentTimeMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : ViewModel() {

    private val selectedSimulator = readoutListUseCases
        .observeSelectedSimulator()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // GT7にはdetailPaneのサブトグルが存在しないため、listPaneの状態のみで完結する。
    private val listEnabledStates = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator == null) emptyFlow() else readoutListUseCases.observeReadoutEnabledStates(simulator.id)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val readoutOrder = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator == null) emptyFlow() else readoutListUseCases.observeReadoutOrder(simulator.id)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // キューに追加して読み上げるかどうか（ReadoutItemKey.TopLevel 単位）。
    private val queueEnabledStates = readoutListUseCases
        .observeQueueEnabledStates()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap<ReadoutItemKey, Boolean>())

    private val voiceType = myBestLapUseCases
        .observeMyBestLapVoiceType()
        .stateIn(viewModelScope, SharingStarted.Eagerly, MyBestLapVoiceType.FORMAL)

    private val fuelThreshold = remainingFuelLapsUseCases
        .observeRemainingFuelLapsThreshold()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 3)

    private val remainingFuelThreshold = remainingFuelUseCases
        .observeRemainingFuelThresholdPercentage()
        .stateIn(viewModelScope, SharingStarted.Eagerly, GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT)

    private var narratorState = Gt7Ps5NarratorState()

    private val currentSettings: Gt7Ps5NarratorReadoutSettings
        get() = Gt7Ps5NarratorReadoutSettings(
            enabledStates = listEnabledStates.value,
            myBestLapVoiceType = voiceType.value,
            remainingFuelLapsThreshold = fuelThreshold.value,
            remainingFuelLapsEnabled = listEnabledStates.value.getValue(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root),
            remainingFuelThresholdPercentage = remainingFuelThreshold.value,
            remainingFuelEnabled = listEnabledStates.value.getValue(ReadoutItemKey.Gt7Ps5.RemainingFuel.Root),
        )

    @Suppress("UnusedPrivateProperty")
    private val readoutJob = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator !is Simulator.Gt7Ps5) emptyFlow()
            else myBestLapUseCases.observeGt7Ps5()
        }.onEach { telemetry ->
            val observedAtMs = currentTimeMs()
            val settings = currentSettings
            val initialState = narratorState
            val myBestLapDecision = determineGt7Ps5NarratorReadout.determineMyBestLap(
                state = initialState,
                telemetry = telemetry,
                settings = settings,
            )
            eventProcessor.process(
                sourceKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root,
                telemetry = telemetry,
                events = myBestLapDecision.events,
                readoutOrder = readoutOrder.value,
                queueEnabledStates = queueEnabledStates.value,
                observedAtMs = observedAtMs,
                logContext = Gt7Ps5TelemetryLogContext(
                    state = initialState,
                    settings = settings,
                    finalState = myBestLapDecision.state,
                ),
            )
            val remainingFuelLapsDecision = determineGt7Ps5NarratorReadout.determineRemainingFuelLaps(
                state = myBestLapDecision.state,
                telemetry = telemetry,
                settings = settings,
                observedAtMs = observedAtMs,
            )
            narratorState = remainingFuelLapsDecision.state
            eventProcessor.process(
                sourceKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root,
                telemetry = telemetry,
                events = remainingFuelLapsDecision.events,
                readoutOrder = readoutOrder.value,
                queueEnabledStates = queueEnabledStates.value,
                observedAtMs = observedAtMs,
                logContext = Gt7Ps5TelemetryLogContext(
                    state = myBestLapDecision.state,
                    settings = settings,
                    finalState = remainingFuelLapsDecision.state,
                ),
            )
            val remainingFuelDecision = determineGt7Ps5NarratorReadout.determineRemainingFuel(
                state = remainingFuelLapsDecision.state,
                telemetry = telemetry,
                settings = settings,
            )
            narratorState = remainingFuelDecision.state
            eventProcessor.process(
                sourceKey = ReadoutItemKey.Gt7Ps5.RemainingFuel.Root,
                telemetry = telemetry,
                events = remainingFuelDecision.events,
                readoutOrder = readoutOrder.value,
                queueEnabledStates = queueEnabledStates.value,
                observedAtMs = observedAtMs,
                logContext = Gt7Ps5TelemetryLogContext(
                    state = remainingFuelLapsDecision.state,
                    settings = settings,
                    finalState = remainingFuelDecision.state,
                ),
            )
        }.launchIn(viewModelScope)
}
