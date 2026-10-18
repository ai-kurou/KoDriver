package kurou.kodriver.feature.acewindowsnarrator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.acewindows.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.acewindows.model.AceWindowsFuelData
import kurou.kodriver.domain.acewindows.model.AceWindowsRemainingFuelLapsData
import kurou.kodriver.domain.acewindows.usecase.AceWindowsNarratorReadoutSettings
import kurou.kodriver.domain.acewindows.usecase.AceWindowsNarratorState
import kurou.kodriver.domain.acewindows.usecase.AceWindowsVehicleApproachThresholdsUseCases
import kurou.kodriver.domain.acewindows.usecase.DetermineAceWindowsNarratorReadoutUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsBestLapTimeUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelLapsThresholdUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelLapsUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsVehicleApproachUseCase
import kurou.kodriver.domain.model.AceWindowsManualReadoutItem
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SELECTED_SIMULATOR_DEFAULT
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.manualReadoutItemTextDefault
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
    val observeBestLapTime: ObserveAceWindowsBestLapTimeUseCase,
)

internal data class RemainingFuelUseCases(
    val observeAceWindowsFuel: ObserveAceWindowsFuelUseCase,
    val observeThresholdPercentage: ObserveAceWindowsRemainingFuelThresholdPercentageUseCase,
)

internal data class RemainingFuelLapsUseCases(
    val observeAceWindowsRemainingFuelLaps: ObserveAceWindowsRemainingFuelLapsUseCase,
    val observeThreshold: ObserveAceWindowsRemainingFuelLapsThresholdUseCase,
)

internal data class FlagUseCases(
    val observeAceWindowsFlag: ObserveAceWindowsFlagUseCase,
    val observeFlagEnabledStates: ObserveAceWindowsFlagEnabledStatesUseCase,
)

internal data class TyreTemperatureUseCases(
    val observeAceWindowsTyreCarcassTemperature: ObserveAceWindowsTyreCarcassTemperatureUseCase,
    val observeHighThreshold: ObserveAceWindowsTyreTemperatureHighThresholdUseCase,
    val observeTyreTemperatureEnabledStates: ObserveAceWindowsTyreTemperatureEnabledStatesUseCase,
)

internal data class VehicleApproachUseCases(
    val observeVehicleApproach: ObserveAceWindowsVehicleApproachUseCase,
    val observeEnabledStates: ObserveAceWindowsVehicleApproachEnabledStatesUseCase,
    val thresholds: AceWindowsVehicleApproachThresholdsUseCases,
)

internal data class SimulatorUseCases(
    val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
)

internal data class ReadoutListUseCases(
    val observeAutoReadoutEnabledStates: ObserveAutoReadoutEnabledStatesUseCase,
    val observeAutoReadoutOrder: ObserveResolvedAutoReadoutOrderUseCase,
    val observeQueueEnabledStates: ObserveQueueEnabledStatesUseCase,
)

@Suppress("LongParameterList")
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
internal class AceWindowsNarratorViewModel(
    myBestLapUseCases: MyBestLapUseCases,
    remainingFuelUseCases: RemainingFuelUseCases,
    remainingFuelLapsUseCases: RemainingFuelLapsUseCases,
    simulatorUseCases: SimulatorUseCases,
    readoutListUseCases: ReadoutListUseCases,
    flagUseCases: FlagUseCases,
    tyreTemperatureUseCases: TyreTemperatureUseCases,
    vehicleApproachUseCases: VehicleApproachUseCases,
    private val eventProcessor: AceWindowsNarratorEventProcessor,
    private val determineAceWindowsNarratorReadout: DetermineAceWindowsNarratorReadoutUseCase =
        DetermineAceWindowsNarratorReadoutUseCase(),
    manualReadoutUseCases: ManualReadoutUseCases,
    observeDebugReadoutRequest: ObserveDebugReadoutRequestUseCase,
    private val currentTimeMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : ViewModel() {
    private val selectedSimulator =
        simulatorUseCases
            .observeSelectedSimulator()
            .stateIn(viewModelScope, SharingStarted.Eagerly, SELECTED_SIMULATOR_DEFAULT)

    private val listEnabledStates =
        selectedSimulator
            .flatMapLatest { simulator ->
                readoutListUseCases.observeAutoReadoutEnabledStates(simulator.id)
            }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    // listPane（listEnabledStates）とdetailPane（flagEnabledStates・tyreTemperatureEnabledStates）を統合した、
    // Narratorの読み上げ判定に実際に使う唯一のenabledStates。
    private val mergedEnabledStates =
        combine(
            listEnabledStates,
            flagUseCases.observeFlagEnabledStates(),
            tyreTemperatureUseCases.observeTyreTemperatureEnabledStates(),
            vehicleApproachUseCases.observeEnabledStates(),
        ) { readoutStates, flagStates, tyreTemperatureStates, vehicleApproachStates ->
            readoutStates + flagStates + tyreTemperatureStates + vehicleApproachStates
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap<ReadoutItemKey, Boolean>())

    private val autoReadoutOrder =
        selectedSimulator
            .flatMapLatest { simulator ->
                readoutListUseCases.observeAutoReadoutOrder(simulator)
            }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // キューに追加して読み上げるかどうか（ReadoutItemKey.TopLevel 単位）。
    private val queueEnabledStates =
        readoutListUseCases
            .observeQueueEnabledStates()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap<ReadoutItemKey, Boolean>())

    private val remainingFuelThreshold =
        remainingFuelUseCases
            .observeThresholdPercentage()
            .stateIn(viewModelScope, SharingStarted.Eagerly, ACE_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT)

    private val remainingFuelLapsThreshold =
        remainingFuelLapsUseCases
            .observeThreshold()
            .stateIn(viewModelScope, SharingStarted.Eagerly, ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT)

    private val tyreTemperatureHighThreshold =
        tyreTemperatureUseCases
            .observeHighThreshold()
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT,
            )

    private val vehicleApproachThreshold =
        vehicleApproachUseCases.thresholds
            .observeThresholdMeters()
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT,
            )

    private var narratorState = AceWindowsNarratorState()
    private var latestFuel: AceWindowsFuelData? = null
    private var latestFuelReceivedAtMs = 0L
    private var latestFuelLaps: AceWindowsRemainingFuelLapsData? = null
    private var latestFuelLapsReceivedAtMs = 0L
    private var latestBestLap: AceWindowsBestLapTimeData? = null
    private var latestBestLapReceivedAtMs = 0L

    private val currentSettings: AceWindowsNarratorReadoutSettings
        get() =
            AceWindowsNarratorReadoutSettings(
                enabledStates = mergedEnabledStates.value,
                remainingFuelThresholdPercentage = remainingFuelThreshold.value,
                tyreTemperatureHighThresholdCelsius = tyreTemperatureHighThreshold.value,
                vehicleApproachThresholdMeters = vehicleApproachThreshold.value,
                remainingFuelLapsThreshold = remainingFuelLapsThreshold.value,
            )

    private val bestLapTimeFlow =
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator !is Simulator.AceWindows) {
                    latestBestLap = null
                    latestBestLapReceivedAtMs = 0L
                    emptyFlow()
                } else {
                    myBestLapUseCases.observeBestLapTime()
                }
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    private val fuelFlow =
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator !is Simulator.AceWindows) {
                    latestFuel = null
                    latestFuelReceivedAtMs = 0L
                    emptyFlow()
                } else {
                    remainingFuelUseCases.observeAceWindowsFuel()
                }
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    private val remainingFuelLapsFlow =
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator !is Simulator.AceWindows) {
                    latestFuelLaps = null
                    latestFuelLapsReceivedAtMs = 0L
                    emptyFlow()
                } else {
                    remainingFuelLapsUseCases.observeAceWindowsRemainingFuelLaps()
                }
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    private val flagFlow =
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator !is Simulator.AceWindows) emptyFlow() else flagUseCases.observeAceWindowsFlag()
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    private val tyreTemperatureFlow =
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator !is Simulator.AceWindows) {
                    emptyFlow()
                } else {
                    tyreTemperatureUseCases.observeAceWindowsTyreCarcassTemperature()
                }
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    private val vehicleApproachFlow =
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator !is Simulator.AceWindows) {
                    emptyFlow()
                } else {
                    vehicleApproachUseCases.observeVehicleApproach()
                }
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    @Suppress("UnusedPrivateProperty")
    private val debugReadoutRequestJob =
        observeDebugReadoutRequest()
            .onEach { request ->
                if (request.simulator == Simulator.AceWindows) {
                    val state = narratorState
                    eventProcessor.processDebugReadout(
                        request = request,
                        autoReadoutOrder = autoReadoutOrder.value,
                        queueEnabledStates = queueEnabledStates.value,
                        observedAtMs = currentTimeMs(),
                        logContext = AceWindowsTelemetryLogContext(state, currentSettings, state),
                    )
                }
            }.launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val manualReadoutRequestJob =
        merge(
            manualReadoutUseCases.observeRequests(),
            manualReadoutUseCases.observeHotkeyPresses(),
        ).onEach { request ->
            if (request.simulatorId == Simulator.AceWindows.id) {
                val set = manualReadoutUseCases.observeSets().first().find { it.id == request.setId }
                readManualReadout(set)
            }
        }.launchIn(viewModelScope)

    private suspend fun readManualReadout(set: ManualReadoutSet?) {
        if (set == null || selectedSimulator.value !is Simulator.AceWindows) return
        val observedAtMs = currentTimeMs()
        val state = narratorState
        val settings = currentSettings
        val fuel = latestFuel?.takeIf { observedAtMs - latestFuelReceivedAtMs <= CURRENT_VALUE_MAX_AGE_MS }
        val fuelLaps = latestFuelLaps?.takeIf { observedAtMs - latestFuelLapsReceivedAtMs <= CURRENT_VALUE_MAX_AGE_MS }
        val percent = determineAceWindowsNarratorReadout.currentRemainingFuelPercent(fuel)
        val laps = determineAceWindowsNarratorReadout.currentRemainingFuelLaps(fuelLaps)
        val bestLap = latestBestLap?.takeIf { observedAtMs - latestBestLapReceivedAtMs <= CURRENT_VALUE_MAX_AGE_MS }
        val bestLapTimeMs = determineAceWindowsNarratorReadout.currentBestLapTimeMs(bestLap)
        val items =
            set.itemKeys.map { key ->
                val value =
                    when (key) {
                        AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value -> laps
                        AceWindowsReadoutItemKey.MyBestLap.Root.value -> bestLapTimeMs
                        else -> percent
                    }
                AceWindowsManualReadoutItem(key, set.itemTexts[key] ?: manualReadoutItemTextDefault(key), value)
            }
        val decision = determineAceWindowsNarratorReadout.determineManualReadout(state, items)
        eventProcessor.processManualReadout(
            events = decision.events,
            autoReadoutOrder = autoReadoutOrder.value,
            queueEnabledStates = queueEnabledStates.value + (decision.events.first().readoutItemKey to true),
            observedAtMs = observedAtMs,
            logContext = AceWindowsTelemetryLogContext(state, settings, decision.state),
        )
    }

    @Suppress("UnusedPrivateProperty")
    private val myBestLapJob =
        bestLapTimeFlow
            .onEach { bestLapTime ->
                val observedAtMs = currentTimeMs()
                latestBestLap = bestLapTime
                latestBestLapReceivedAtMs = observedAtMs
                val state = narratorState
                val settings = currentSettings
                val decision =
                    determineAceWindowsNarratorReadout.determineMyBestLap(
                        state = state,
                        data = bestLapTime,
                        settings = settings,
                    )
                narratorState = decision.state
                eventProcessor.processMyBestLap(
                    bestLapTime = bestLapTime,
                    events = decision.events,
                    autoReadoutOrder = autoReadoutOrder.value,
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
    private val fuelJob =
        fuelFlow
            .onEach { fuel ->
                val observedAtMs = currentTimeMs()
                latestFuel = fuel
                latestFuelReceivedAtMs = observedAtMs
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
                    autoReadoutOrder = autoReadoutOrder.value,
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
    private val remainingFuelLapsJob =
        remainingFuelLapsFlow
            .onEach { remainingFuelLaps ->
                val observedAtMs = currentTimeMs()
                latestFuelLaps = remainingFuelLaps
                latestFuelLapsReceivedAtMs = observedAtMs
                val state = narratorState
                val settings = currentSettings
                val decision =
                    determineAceWindowsNarratorReadout.determineRemainingFuelLaps(
                        state = state,
                        data = remainingFuelLaps,
                        settings = settings,
                    )
                narratorState = decision.state
                eventProcessor.processRemainingFuelLaps(
                    remainingFuelLaps = remainingFuelLaps,
                    events = decision.events,
                    autoReadoutOrder = autoReadoutOrder.value,
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
                    autoReadoutOrder = autoReadoutOrder.value,
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
    private val tyreTemperatureJob =
        tyreTemperatureFlow
            .onEach { tyreCarcassTemperature ->
                val observedAtMs = currentTimeMs()
                val state = narratorState
                val settings = currentSettings
                val decision =
                    determineAceWindowsNarratorReadout.determineTyreTemperatureOverheat(
                        state = state,
                        data = tyreCarcassTemperature,
                        settings = settings,
                    )
                narratorState = decision.state
                eventProcessor.processTyreTemperature(
                    tyreCarcassTemperature = tyreCarcassTemperature,
                    events = decision.events,
                    autoReadoutOrder = autoReadoutOrder.value,
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
    private val vehicleApproachJob =
        vehicleApproachFlow
            .onEach { vehicleApproach ->
                val observedAtMs = currentTimeMs()
                val state = narratorState
                val settings = currentSettings
                val decision =
                    determineAceWindowsNarratorReadout.determineVehicleApproach(
                        state = state,
                        data = vehicleApproach,
                        settings = settings,
                    )
                narratorState = decision.state
                eventProcessor.processVehicleApproach(
                    vehicleApproach = vehicleApproach,
                    events = decision.events,
                    autoReadoutOrder = autoReadoutOrder.value,
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

private const val CURRENT_VALUE_MAX_AGE_MS = 5_000L
