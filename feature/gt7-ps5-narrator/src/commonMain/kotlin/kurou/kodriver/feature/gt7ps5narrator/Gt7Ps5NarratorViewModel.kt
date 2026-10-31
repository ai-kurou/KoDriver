package kurou.kodriver.feature.gt7ps5narrator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.gt7ps5.model.gt7Ps5ManualReadoutItemTextDefault
import kurou.kodriver.domain.gt7ps5.usecase.DetermineGt7Ps5NarratorReadoutUseCase
import kurou.kodriver.domain.gt7ps5.usecase.Gt7Ps5NarratorReadoutSettings
import kurou.kodriver.domain.gt7ps5.usecase.Gt7Ps5NarratorState
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5TyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5TyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.model.Gt7Ps5ManualReadoutItem
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.ManualReadoutValue
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SELECTED_SIMULATOR_DEFAULT
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveAutoReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveDebugReadoutRequestUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutHotkeyPressesUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutRequestsUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutSetsUseCase
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveResolvedAutoReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal data class ManualReadoutUseCases(
    val observeRequests: ObserveManualReadoutRequestsUseCase,
    val observeHotkeyPresses: ObserveManualReadoutHotkeyPressesUseCase,
    val observeSets: ObserveManualReadoutSetsUseCase,
)

internal data class MyBestLapUseCases(
    val observeGt7Ps5: ObserveGt7Ps5UseCase,
)

internal data class SimulatorUseCases(
    val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
)

internal data class ReadoutListUseCases(
    val observeAutoReadoutEnabledStates: ObserveAutoReadoutEnabledStatesUseCase,
    val observeAutoReadoutOrder: ObserveResolvedAutoReadoutOrderUseCase,
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

@OptIn(ExperimentalTime::class)
@Suppress("LongParameterList")
internal class Gt7Ps5NarratorViewModel(
    myBestLapUseCases: MyBestLapUseCases,
    simulatorUseCases: SimulatorUseCases,
    readoutListUseCases: ReadoutListUseCases,
    remainingFuelLapsUseCases: RemainingFuelLapsUseCases,
    remainingFuelUseCases: RemainingFuelUseCases,
    tyreTemperatureUseCases: TyreTemperatureUseCases,
    private val eventProcessor: Gt7Ps5NarratorEventProcessor,
    private val determineGt7Ps5NarratorReadout: DetermineGt7Ps5NarratorReadoutUseCase =
        DetermineGt7Ps5NarratorReadoutUseCase(),
    manualReadoutUseCases: ManualReadoutUseCases,
    observeDebugReadoutRequest: ObserveDebugReadoutRequestUseCase,
    private val currentTimeMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : ViewModel() {
    private val selectedSimulator =
        simulatorUseCases
            .observeSelectedSimulator()
            .stateIn(viewModelScope, SharingStarted.Eagerly, SELECTED_SIMULATOR_DEFAULT)

    private val narratorFlow = Gt7Ps5NarratorFlow(selectedSimulator)

    private val mergedEnabledStates =
        narratorFlow
            .enabledStates(
                observeAutoReadoutEnabledStates = { simulatorId ->
                    readoutListUseCases.observeAutoReadoutEnabledStates(simulatorId)
                },
                tyreTemperatureStates = tyreTemperatureUseCases.observeTyreTemperatureEnabledStates(),
            ).stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val autoReadoutOrder =
        narratorFlow
            .autoReadoutOrder { simulator -> readoutListUseCases.observeAutoReadoutOrder(simulator) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // キューに追加して読み上げるかどうか（ReadoutItemKey.TopLevel 単位）。
    private val queueEnabledStates =
        readoutListUseCases
            .observeQueueEnabledStates()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap<ReadoutItemKey, Boolean>())

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
    private var latestTelemetry: Gt7Ps5TelemetryData? = null
    private var latestTelemetryReceivedAtMs = 0L

    @Suppress("UnusedPrivateProperty")
    private val resetLatestTelemetryJob =
        selectedSimulator
            .onEach { simulator ->
                if (simulator !is Simulator.Gt7Ps5) {
                    resetLatestTelemetry()
                }
            }.launchIn(viewModelScope)

    private fun resetLatestTelemetry() {
        latestTelemetry = null
        latestTelemetryReceivedAtMs = 0L
    }

    private val telemetryFlow =
        narratorFlow
            .telemetry { myBestLapUseCases.observeGt7Ps5() }
            .shareIn(viewModelScope, SharingStarted.Eagerly)

    @Suppress("UnusedPrivateProperty")
    private val debugReadoutRequestJob =
        observeDebugReadoutRequest()
            .onEach { request ->
                if (request.simulator == Simulator.Gt7Ps5) {
                    val state = narratorState
                    eventProcessor.processDebugReadout(
                        request = request,
                        autoReadoutOrder = autoReadoutOrder.value,
                        queueEnabledStates = queueEnabledStates.value,
                        observedAtMs = currentTimeMs(),
                        logContext = Gt7Ps5TelemetryLogContext(state, currentSettings, state),
                    )
                }
            }.launchIn(viewModelScope)

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
                    sourceKey = Gt7Ps5ReadoutItemKey.MyBestLap.Root,
                    telemetry = telemetry,
                    events = decision.events,
                    autoReadoutOrder = autoReadoutOrder.value,
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
                    sourceKey = Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
                    telemetry = telemetry,
                    events = decision.events,
                    autoReadoutOrder = autoReadoutOrder.value,
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
                latestTelemetry = telemetry
                latestTelemetryReceivedAtMs = observedAtMs
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
                    sourceKey = Gt7Ps5ReadoutItemKey.RemainingFuel.Root,
                    telemetry = telemetry,
                    events = decision.events,
                    autoReadoutOrder = autoReadoutOrder.value,
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
                    sourceKey = Gt7Ps5ReadoutItemKey.TyreTemperature.Root,
                    telemetry = telemetry,
                    events = decision.events,
                    autoReadoutOrder = autoReadoutOrder.value,
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
    private val manualReadoutRequestJob =
        narratorFlow
            .manualReadoutRequests(
                manualReadoutUseCases.observeRequests(),
                manualReadoutUseCases.observeHotkeyPresses(),
            ).onEach { request ->
                if (request.simulatorId == Simulator.Gt7Ps5.id) {
                    val set = manualReadoutUseCases.observeSets().first().find { it.id == request.setId }
                    readManualReadout(set)
                }
            }.launchIn(viewModelScope)

    private fun manualReadoutValue(
        key: String,
        telemetry: Gt7Ps5TelemetryData,
        state: Gt7Ps5NarratorState,
    ): ManualReadoutValue? =
        when (key) {
            Gt7Ps5ReadoutItemKey.TyreTemperature.Root.value -> {
                determineGt7Ps5NarratorReadout.currentMaxTyreTemperatureCelsius(telemetry)?.let {
                    ManualReadoutValue.Celsius(it)
                }
            }

            Gt7Ps5ReadoutItemKey.MyBestLap.Root.value -> {
                determineGt7Ps5NarratorReadout.currentBestLapTimeMs(telemetry)?.let { ManualReadoutValue.LapTimeMs(it) }
            }

            Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value -> {
                determineGt7Ps5NarratorReadout
                    .currentRemainingFuelLaps(
                        state,
                    )?.let { ManualReadoutValue.RemainingLaps(it) }
            }

            else -> {
                determineGt7Ps5NarratorReadout
                    .currentRemainingFuelPercent(
                        telemetry,
                    )?.let { ManualReadoutValue.Percent(it) }
            }
        }

    private suspend fun readManualReadout(set: ManualReadoutSet?) {
        if (set == null || selectedSimulator.value !is Simulator.Gt7Ps5) return
        val observedAtMs = currentTimeMs()
        val state = narratorState
        val settings = currentSettings
        val telemetry =
            latestTelemetry?.takeIf { observedAtMs - latestTelemetryReceivedAtMs <= CURRENT_VALUE_MAX_AGE_MS }
        val items =
            set.itemKeys.map { key ->
                val value = telemetry?.let { manualReadoutValue(key, it, state) }
                Gt7Ps5ManualReadoutItem(
                    key,
                    set.itemTexts[key] ?: gt7Ps5ManualReadoutItemTextDefault(key),
                    value,
                )
            }
        val decision = determineGt7Ps5NarratorReadout.determineManualReadout(state, items)
        eventProcessor.processManualReadout(
            events = decision.events,
            autoReadoutOrder = autoReadoutOrder.value,
            queueEnabledStates = queueEnabledStates.value + (decision.events.first().readoutItemKey to true),
            observedAtMs = observedAtMs,
            logContext = Gt7Ps5TelemetryLogContext(state, settings, decision.state),
        )
    }

    private val currentSettings: Gt7Ps5NarratorReadoutSettings
        get() =
            Gt7Ps5NarratorReadoutSettings(
                enabledStates = mergedEnabledStates.value,
                remainingFuelLapsThreshold = fuelThreshold.value,
                remainingFuelThresholdPercentage = remainingFuelThreshold.value,
                tyreTemperatureHighThresholdCelsius = tyreTemperatureHighThreshold.value,
            )
}

private const val CURRENT_VALUE_MAX_AGE_MS = 5_000L
