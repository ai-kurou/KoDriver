package kurou.kodriver.feature.gt7ps5narrator

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
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.MY_BEST_LAP_VOICE_TYPE_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SELECTED_SIMULATOR_DEFAULT
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.DetermineGt7Ps5NarratorReadoutUseCase
import kurou.kodriver.domain.usecase.Gt7Ps5NarratorReadoutSettings
import kurou.kodriver.domain.usecase.Gt7Ps5NarratorState
import kurou.kodriver.domain.usecase.ObserveGt7Ps5MyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureHighThresholdUseCase
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

internal data class TyreTemperatureUseCases(
    val observeHighThresholdCelsius: ObserveGt7Ps5TyreTemperatureHighThresholdUseCase,
    val observeTyreTemperatureEnabledStates: ObserveGt7Ps5TyreTemperatureEnabledStatesUseCase,
)

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
@Suppress("LongParameterList")
internal class Gt7Ps5NarratorViewModel(
    myBestLapUseCases: MyBestLapUseCases,
    readoutListUseCases: ReadoutListUseCases,
    remainingFuelLapsUseCases: RemainingFuelLapsUseCases,
    remainingFuelUseCases: RemainingFuelUseCases,
    tyreTemperatureUseCases: TyreTemperatureUseCases,
    private val eventProcessor: Gt7Ps5NarratorEventProcessor,
    private val determineGt7Ps5NarratorReadout: DetermineGt7Ps5NarratorReadoutUseCase =
        DetermineGt7Ps5NarratorReadoutUseCase(),
    private val currentTimeMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : ViewModel() {
    private val selectedSimulator =
        readoutListUseCases
            .observeSelectedSimulator()
            .stateIn(viewModelScope, SharingStarted.Eagerly, SELECTED_SIMULATOR_DEFAULT)

    // listPane（readoutStates）とdetailPane（tyreTemperatureStates）を統合した、
    // Narratorの読み上げ判定に実際に使う唯一のenabledStates。
    private val mergedEnabledStates =
        combine(
            selectedSimulator
                .flatMapLatest { simulator ->
                    readoutListUseCases.observeReadoutEnabledStates(simulator.id)
                },
            tyreTemperatureUseCases.observeTyreTemperatureEnabledStates(),
        ) { readoutStates, tyreTemperatureStates ->
            readoutStates + tyreTemperatureStates
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val readoutOrder =
        selectedSimulator
            .flatMapLatest { simulator ->
                readoutListUseCases.observeReadoutOrder(simulator.id)
            }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // キューに追加して読み上げるかどうか（ReadoutItemKey.TopLevel 単位）。
    private val queueEnabledStates =
        readoutListUseCases
            .observeQueueEnabledStates()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap<ReadoutItemKey, Boolean>())

    private val voiceType =
        myBestLapUseCases
            .observeMyBestLapVoiceType()
            .stateIn(viewModelScope, SharingStarted.Eagerly, MY_BEST_LAP_VOICE_TYPE_DEFAULT)

    private val fuelThreshold =
        remainingFuelLapsUseCases
            .observeRemainingFuelLapsThreshold()
            .stateIn(viewModelScope, SharingStarted.Eagerly, GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT)

    private val remainingFuelThreshold =
        remainingFuelUseCases
            .observeRemainingFuelThresholdPercentage()
            .stateIn(viewModelScope, SharingStarted.Eagerly, GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT)

    private val tyreTemperatureHighThreshold =
        tyreTemperatureUseCases
            .observeHighThresholdCelsius()
            .stateIn(viewModelScope, SharingStarted.Eagerly, GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT)

    private var narratorState = Gt7Ps5NarratorState()

    private val telemetryFlow =
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator !is Simulator.Gt7Ps5) {
                    emptyFlow()
                } else {
                    myBestLapUseCases.observeGt7Ps5()
                }
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    @Suppress("UnusedPrivateProperty")
    private val myBestLapJob =
        telemetryFlow
            .onEach { telemetry ->
                val observedAtMs = currentTimeMs()
                val state = narratorState
                val settings = currentSettings
                val decision =
                    determineGt7Ps5NarratorReadout.determineMyBestLap(
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
                    logContext =
                        Gt7Ps5TelemetryLogContext(
                            state = state,
                            settings = settings,
                            finalState = decision.state,
                        ),
                )
            }.launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val remainingFuelLapsJob =
        telemetryFlow
            .onEach { telemetry ->
                val observedAtMs = currentTimeMs()
                val state = narratorState
                val settings = currentSettings
                val decision =
                    determineGt7Ps5NarratorReadout.determineRemainingFuelLaps(
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
                    logContext =
                        Gt7Ps5TelemetryLogContext(
                            state = state,
                            settings = settings,
                            finalState = decision.state,
                        ),
                )
            }.launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val remainingFuelJob =
        telemetryFlow
            .onEach { telemetry ->
                val observedAtMs = currentTimeMs()
                val state = narratorState
                val settings = currentSettings
                val decision =
                    determineGt7Ps5NarratorReadout.determineRemainingFuel(
                        state = state,
                        telemetry = telemetry,
                        settings = settings,
                    )
                narratorState = decision.state
                eventProcessor.process(
                    sourceKey = ReadoutItemKey.Gt7Ps5.RemainingFuel.Root,
                    telemetry = telemetry,
                    events = decision.events,
                    readoutOrder = readoutOrder.value,
                    queueEnabledStates = queueEnabledStates.value,
                    observedAtMs = observedAtMs,
                    logContext =
                        Gt7Ps5TelemetryLogContext(
                            state = state,
                            settings = settings,
                            finalState = decision.state,
                        ),
                )
            }.launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val tyreTemperatureJob =
        telemetryFlow
            .onEach { telemetry ->
                val observedAtMs = currentTimeMs()
                val state = narratorState
                val settings = currentSettings
                val decision =
                    determineGt7Ps5NarratorReadout.determineTyreTemperature(
                        state = state,
                        telemetry = telemetry,
                        settings = settings,
                    )
                narratorState = decision.state
                eventProcessor.process(
                    sourceKey = ReadoutItemKey.Gt7Ps5.TyreTemperature.Root,
                    telemetry = telemetry,
                    events = decision.events,
                    readoutOrder = readoutOrder.value,
                    queueEnabledStates = queueEnabledStates.value,
                    observedAtMs = observedAtMs,
                    logContext =
                        Gt7Ps5TelemetryLogContext(
                            state = state,
                            settings = settings,
                            finalState = decision.state,
                        ),
                )
            }.launchIn(viewModelScope)

    private val currentSettings: Gt7Ps5NarratorReadoutSettings
        get() =
            Gt7Ps5NarratorReadoutSettings(
                enabledStates = mergedEnabledStates.value,
                myBestLapVoiceType = voiceType.value,
                remainingFuelLapsThreshold = fuelThreshold.value,
                remainingFuelThresholdPercentage = remainingFuelThreshold.value,
                tyreTemperatureHighThresholdCelsius = tyreTemperatureHighThreshold.value,
            )
}
