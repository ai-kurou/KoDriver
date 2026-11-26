package kurou.kodriver.feature.lmuwindowsnarrator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.debug.usecase.ObserveDebugReadoutRequestUseCase
import kurou.kodriver.domain.engine.LmuWindowsPitTimingWarning
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_PIT_TIMING_FUEL_LAPS_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_APPROACH_SKIP_FIRST_LAP_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_CLASS_UNKNOWN_KEY
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsClassCarGapData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsEnergySource
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRemainingFuelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsTyreTemperatureLowWarningDefaultPhases
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault
import kurou.kodriver.domain.lmuwindows.usecase.LmuWindowsNarratorReadoutDecision
import kurou.kodriver.domain.lmuwindows.usecase.LmuWindowsNarratorReadoutSettings
import kurou.kodriver.domain.lmuwindows.usecase.LmuWindowsNarratorState
import kurou.kodriver.domain.lmuwindows.usecase.LmuWindowsPitTimingTrackingState
import kurou.kodriver.domain.lmuwindows.usecase.TyreTemperatureReadoutInput
import kurou.kodriver.domain.manualreadout.usecase.mergeManualReadoutRequests
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.NARRATOR_CURRENT_VALUE_MAX_AGE_MS
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SELECTED_SIMULATOR_DEFAULT
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.onEachCapturingErrors
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Suppress("LongParameterList")
internal class LmuWindowsNarratorViewModel(
    vehicleApproachUseCases: VehicleApproachUseCases,
    vehicleDamageUseCases: VehicleDamageUseCases,
    tyreDetachedUseCases: TyreDetachedUseCases,
    simulatorUseCases: SimulatorUseCases,
    readoutListUseCases: ReadoutListUseCases,
    flagUseCases: FlagUseCases,
    tyreTemperatureUseCases: TyreTemperatureUseCases,
    tyreWearUseCases: TyreWearUseCases,
    brakeTemperatureUseCases: BrakeTemperatureUseCases,
    brakeWearUseCases: BrakeWearUseCases,
    remainingVirtualEnergyUseCases: RemainingVirtualEnergyUseCases,
    remainingFuelUseCases: RemainingFuelUseCases,
    pitTimingUseCases: PitTimingUseCases,
    classCarGapUseCases: ClassCarGapUseCases,
    private val eventProcessor: LmuWindowsNarratorEventProcessor,
    private val narratorUseCases: NarratorUseCases,
    observeDebugReadoutRequest: ObserveDebugReadoutRequestUseCase,
    private val currentTimeMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : ViewModel() {
    private var narratorState = LmuWindowsNarratorState()
    private val manualReadoutComposer = LmuWindowsManualReadoutComposer(narratorUseCases.manualReadout)

    // ピットタイミング警告は同一ラップ内で予想残り周回数が最も低いものだけを読み上げる（別tickの後着もより緊急なら読み上げる）。
    private val pitTimingLapGate = PitTimingLapGate()

    private val selectedSimulator =
        simulatorUseCases
            .observeSelectedSimulator()
            .stateIn(viewModelScope, SharingStarted.Eagerly, SELECTED_SIMULATOR_DEFAULT)

    private val narratorFlow = LmuWindowsNarratorFlow(selectedSimulator)

    // listPane（readoutStates）とdetailPane（flagStates・vehicleDamageStates）を統合した、
    // Narratorの読み上げ判定に実際に使う唯一のenabledStates。
    private val mergedEnabledStates =
        narratorFlow
            .mergedEnabledStates(
                readoutListUseCases.observeAutoReadoutEnabledStates::invoke,
                flagUseCases.observeFlagEnabledStates(),
                vehicleDamageUseCases.observeVehicleDamageEnabledStates(),
                tyreTemperatureUseCases.observeTyreTemperatureEnabledStates(),
                vehicleApproachUseCases.observeEnabledStates(),
                pitTimingUseCases.observeEnabledStates(),
            ).stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap<ReadoutItemKey, Boolean>())

    // index が小さいほど優先度が高い（リスト上位 = 高優先）
    private val autoReadoutOrder =
        narratorFlow
            .autoReadoutOrder(readoutListUseCases.observeAutoReadoutOrder::invoke)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // キューに追加して読み上げるかどうか（ReadoutItemKey.TopLevel 単位）。
    private val queueEnabledStates =
        readoutListUseCases
            .observeQueueEnabledStates()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap<ReadoutItemKey, Boolean>())

    private var latestBestLapTimeMs: Long? = null
    private var latestBestLapReceivedAtMs: Long = 0L
    private var latestPitTimingUpdatedAtMs: Long = 0L

    private val lmuTelemetryFlow =
        narratorFlow
            .telemetry {
                vehicleApproachUseCases.observeLmuWindows().onEach {
                    latestBestLapTimeMs = it.timing.bestLapTimeMs
                    latestBestLapReceivedAtMs = currentTimeMs()
                }
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    private val raceFlagsFlow =
        narratorFlow
            .telemetry {
                flagUseCases.observeRaceFlags()
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    private val currentLap =
        narratorFlow
            .currentLap(lmuTelemetryFlow)
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val vehicleClassFlow =
        narratorFlow
            .telemetry {
                tyreTemperatureUseCases.observeVehicleClass()
            }

    private val energySourceFlow =
        narratorFlow
            .energySource(vehicleClassFlow)
            .stateIn(viewModelScope, SharingStarted.Eagerly, LmuWindowsEnergySource.Fuel)

    private val tyreHighThreshold =
        narratorFlow
            .tyreHighThreshold(
                vehicleClassFlow,
                tyreTemperatureUseCases.observeVehicleClassHighThreshold(),
            ).stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(
                    LmuWindowsVehicleClassData.Unknown(LMU_WINDOWS_VEHICLE_CLASS_UNKNOWN_KEY),
                ),
            )

    private val tyreLowWarningPhases =
        tyreTemperatureUseCases
            .observeLowWarningPhases()
            .stateIn(viewModelScope, SharingStarted.Eagerly, lmuWindowsTyreTemperatureLowWarningDefaultPhases)

    private val tyreWearThresholdPercentage =
        tyreWearUseCases
            .observeThresholdPercentage()
            .stateIn(viewModelScope, SharingStarted.Eagerly, LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT)

    private val brakeTemperatureHighThresholdCelsius =
        narratorFlow
            .brakeHighThreshold(
                vehicleClassFlow,
                brakeTemperatureUseCases.observeVehicleClassHighThreshold(),
            ).stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                Celsius(
                    lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(
                        LmuWindowsVehicleClassData.Unknown(LMU_WINDOWS_VEHICLE_CLASS_UNKNOWN_KEY),
                    ),
                ),
            )

    private val brakeWearLowThresholdPercent =
        narratorFlow
            .brakeLowThreshold(
                vehicleClassFlow,
                brakeWearUseCases.observeVehicleClassLowThreshold(),
            ).stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault(
                    LmuWindowsVehicleClassData.Unknown(LMU_WINDOWS_VEHICLE_CLASS_UNKNOWN_KEY),
                ),
            )

    private val remainingVirtualEnergyThresholdPercentage =
        remainingVirtualEnergyUseCases
            .observeThresholdPercentage()
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_THRESHOLD_PERCENTAGE_DEFAULT,
            )

    private val remainingFuelThresholdPercentage =
        remainingFuelUseCases
            .observeThresholdPercentage()
            .stateIn(viewModelScope, SharingStarted.Eagerly, LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT)

    private val pitTimingVirtualEnergyLapsThreshold =
        pitTimingUseCases
            .observeVirtualEnergyLapsThreshold()
            .stateIn(viewModelScope, SharingStarted.Eagerly, LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT)

    private val pitTimingTyreWearLapsThreshold =
        pitTimingUseCases
            .observeTyreWearLapsThreshold()
            .stateIn(viewModelScope, SharingStarted.Eagerly, LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT)

    private val pitTimingFuelLapsThreshold =
        pitTimingUseCases
            .observeFuelLapsThreshold()
            .stateIn(viewModelScope, SharingStarted.Eagerly, LMU_WINDOWS_PIT_TIMING_FUEL_LAPS_DEFAULT)

    private val skipFirstLap =
        vehicleApproachUseCases
            .observeSkipFirstLap()
            .stateIn(viewModelScope, SharingStarted.Eagerly, LMU_WINDOWS_VEHICLE_APPROACH_SKIP_FIRST_LAP_DEFAULT)

    private val sustainedApproachDurationSeconds =
        vehicleApproachUseCases
            .observeSustainedApproachDuration()
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT,
            )

    @Suppress("UnusedPrivateProperty")
    private val debugReadoutRequestJob =
        observeDebugReadoutRequest()
            .onEachCapturingErrors { request ->
                if (request.simulator == Simulator.LmuWindows) {
                    val state = narratorState
                    eventProcessor.processDebugReadout(
                        request = request,
                        autoReadoutOrder = autoReadoutOrder.value,
                        queueEnabledStates = queueEnabledStates.value,
                        observedAtMs = currentTimeMs(),
                        logContext = LmuWindowsTelemetryLogContext(state, currentSettings, state),
                    )
                }
            }.launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val myBestLapJob =
        lmuTelemetryFlow
            .onEachCapturingErrors { telemetry ->
                val observedAtMs = currentTimeMs()
                val state = narratorState
                val settings = currentSettings
                val decision =
                    narratorUseCases.myBestLapReadout.determineMyBestLap(
                        state = state,
                        telemetry = telemetry,
                        settings = settings,
                    )
                narratorState = decision.state
                eventProcessor.processMyBestLap(
                    telemetry = telemetry,
                    events = decision.events,
                    autoReadoutOrder = autoReadoutOrder.value,
                    queueEnabledStates = queueEnabledStates.value,
                    observedAtMs = observedAtMs,
                    logContext =
                        LmuWindowsTelemetryLogContext(
                            state = state,
                            settings = settings,
                            finalState = decision.state,
                        ),
                )
            }.launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val vehicleApproachJob =
        narratorFlow
            .telemetry {
                vehicleApproachUseCases.observeVehicleApproach()
            }.onEachCapturingErrors { vehicleApproach ->
                val observedAtMs = currentTimeMs()
                val state = narratorState
                val settings = currentSettings
                val decision =
                    narratorUseCases.vehicleReadout.determineVehicleApproach(
                        state = state,
                        vehicleApproach = vehicleApproach,
                        settings = settings,
                        observedAtMs = observedAtMs,
                    )
                narratorState = decision.state
                eventProcessor.processVehicleApproach(
                    vehicleApproach = vehicleApproach,
                    events = decision.events,
                    autoReadoutOrder = autoReadoutOrder.value,
                    queueEnabledStates = queueEnabledStates.value,
                    observedAtMs = observedAtMs,
                    logContext =
                        LmuWindowsTelemetryLogContext(
                            state = state,
                            settings = settings,
                            finalState = decision.state,
                        ),
                )
            }.launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val vehicleDamageJob =
        narratorFlow
            .telemetry {
                vehicleDamageUseCases.observeVehicleDamage()
            }.onEachCapturingErrors { vehicleDamage ->
                val observedAtMs = currentTimeMs()
                val state = narratorState
                val settings = currentSettings
                val decision =
                    narratorUseCases.vehicleReadout.determineVehicleDamage(
                        state = state,
                        vehicleDamage = vehicleDamage,
                        settings = settings,
                    )
                narratorState = decision.state
                eventProcessor.processVehicleDamage(
                    vehicleDamage = vehicleDamage,
                    events = decision.events,
                    autoReadoutOrder = autoReadoutOrder.value,
                    queueEnabledStates = queueEnabledStates.value,
                    observedAtMs = observedAtMs,
                    logContext =
                        LmuWindowsTelemetryLogContext(
                            state = state,
                            settings = settings,
                            finalState = decision.state,
                        ),
                )
            }.launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val tyreDetachedJob =
        narratorFlow
            .telemetry {
                tyreDetachedUseCases.observeTyreDetached()
            }.onEachCapturingErrors { tyreDetached ->
                val observedAtMs = currentTimeMs()
                val state = narratorState
                val settings = currentSettings
                val decision =
                    narratorUseCases.vehicleReadout.determineTyreDetached(
                        state = state,
                        tyreDetached = tyreDetached,
                        settings = settings,
                    )
                narratorState = decision.state
                eventProcessor.processTyreDetached(
                    tyreDetached = tyreDetached,
                    events = decision.events,
                    autoReadoutOrder = autoReadoutOrder.value,
                    queueEnabledStates = queueEnabledStates.value,
                    observedAtMs = observedAtMs,
                    logContext =
                        LmuWindowsTelemetryLogContext(
                            state = state,
                            settings = settings,
                            finalState = decision.state,
                        ),
                )
            }.launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val flagJob =
        raceFlagsFlow
            .onEachCapturingErrors { raceFlags ->
                val observedAtMs = currentTimeMs()
                val state = narratorState
                val settings = currentSettings
                val decision =
                    narratorUseCases.raceFlagReadout.determineRaceFlags(
                        state = state,
                        raceFlags = raceFlags,
                        settings = settings,
                    )
                narratorState = decision.state
                eventProcessor.processRaceFlags(
                    raceFlags = raceFlags,
                    events = decision.events,
                    autoReadoutOrder = autoReadoutOrder.value,
                    queueEnabledStates = queueEnabledStates.value,
                    observedAtMs = observedAtMs,
                    logContext =
                        LmuWindowsTelemetryLogContext(
                            state = state,
                            settings = settings,
                            finalState = decision.state,
                        ),
                )
            }.launchIn(viewModelScope)

    private var latestTyreTemperature: LmuWindowsTyreCarcassTemperatureData? = null
    private var latestTyreTemperatureReceivedAtMs: Long = 0L

    @Suppress("UnusedPrivateProperty")
    private val tyreTemperatureJob =
        narratorFlow
            .telemetry {
                narratorFlow.tyreTemperature(
                    tyreTemperatureUseCases
                        .observeTyreCarcassTemperature()
                        .onEach {
                            latestTyreTemperature = it
                            latestTyreTemperatureReceivedAtMs = currentTimeMs()
                        },
                    raceFlagsFlow,
                )
            }.onEachCapturingErrors { (tyreCarcassTemperature, raceFlags) ->
                val observedAtMs = currentTimeMs()
                val state = narratorState
                val settings = currentSettings
                val input = TyreTemperatureReadoutInput(tyreCarcassTemperature, raceFlags)
                val overheatDecision =
                    narratorUseCases.tyreReadout.determineTyreTemperatureOverheat(
                        state = state,
                        input = input,
                        settings = settings,
                    )
                narratorState = overheatDecision.state
                val lowDecision =
                    narratorUseCases.tyreReadout.determineTyreTemperatureLow(
                        state = overheatDecision.state,
                        input = input,
                        settings = settings,
                    )
                narratorState = lowDecision.state
                eventProcessor.processTyreTemperature(
                    input = input,
                    events = overheatDecision.events + lowDecision.events,
                    autoReadoutOrder = autoReadoutOrder.value,
                    queueEnabledStates = queueEnabledStates.value,
                    observedAtMs = observedAtMs,
                    logContext =
                        LmuWindowsTyreTemperatureLogContext(
                            state = state,
                            settings = settings,
                            overheatState = overheatDecision.state,
                            finalState = lowDecision.state,
                        ),
                )
            }.launchIn(viewModelScope)

    private var latestTyreWear: LmuWindowsTyreWearData? = null
    private var latestTyreWearReceivedAtMs: Long = 0L

    private val tyreWearFlow =
        narratorFlow
            .telemetry {
                tyreWearUseCases.observeTyreWear().onEach {
                    latestTyreWear = it
                    latestTyreWearReceivedAtMs = currentTimeMs()
                }
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    private var latestBrakeTemperature: LmuWindowsBrakeTemperatureData? = null
    private var latestBrakeTemperatureReceivedAtMs: Long = 0L

    private val brakeTemperatureFlow =
        narratorFlow
            .telemetry {
                brakeTemperatureUseCases.observeBrakeTemperature().onEach {
                    latestBrakeTemperature = it
                    latestBrakeTemperatureReceivedAtMs = currentTimeMs()
                }
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    private var latestBrakeWear: LmuWindowsBrakeWearRemainingData? = null
    private var latestBrakeWearReceivedAtMs: Long = 0L

    private val brakeWearFlow =
        narratorFlow
            .telemetry {
                brakeWearUseCases
                    .observeBrakeWear()
                    .onEach {
                        latestBrakeWear = it
                        latestBrakeWearReceivedAtMs = currentTimeMs()
                    }.filterNotNull()
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    // replay なしの共有 Flow を再購読せず、共有前に最新値と受信時刻を保持する。切り替え時は破棄する。
    private var latestFuel: LmuWindowsRemainingFuelData? = null
    private var latestFuelReceivedAtMs: Long = 0L
    private var latestClassCarGap: LmuWindowsClassCarGapData? = null
    private var latestClassCarGapReceivedAtMs: Long = 0L
    private var latestVirtualEnergy: LmuWindowsVirtualEnergyData? = null
    private var latestVirtualEnergyReceivedAtMs: Long = 0L

    @Suppress("UnusedPrivateProperty")
    private val resetLatestTelemetryJob =
        selectedSimulator
            .onEachCapturingErrors { simulator ->
                if (simulator !is Simulator.LmuWindows) {
                    resetLatestTelemetry()
                }
            }.launchIn(viewModelScope)

    private fun resetLatestTelemetry() {
        latestBestLapTimeMs = null
        latestBestLapReceivedAtMs = 0L
        latestTyreTemperature = null
        latestTyreTemperatureReceivedAtMs = 0L
        latestTyreWear = null
        latestTyreWearReceivedAtMs = 0L
        latestBrakeTemperature = null
        latestBrakeTemperatureReceivedAtMs = 0L
        latestBrakeWear = null
        latestBrakeWearReceivedAtMs = 0L
        latestFuel = null
        latestFuelReceivedAtMs = 0L
        latestClassCarGap = null
        latestClassCarGapReceivedAtMs = 0L
        latestVirtualEnergy = null
        latestVirtualEnergyReceivedAtMs = 0L
        // LMU の選択解除で消費量と自動警告の評価状態を破棄して別セッションへ持ち越さない。
        narratorState =
            narratorState.copy(
                pitTimingVirtualEnergyTrackingState = LmuWindowsPitTimingTrackingState(),
                lastAnnouncedPitTimingVirtualEnergyLaps = -1,
                lastPitTimingVirtualEnergyEvaluationLap = -1,
                pitTimingTyreWearTrackingState = LmuWindowsPitTimingTrackingState(),
                lastAnnouncedPitTimingTyreWearLaps = -1,
                lastPitTimingTyreWearEvaluationLap = -1,
                fuelLapsTrackingState = LmuWindowsPitTimingTrackingState(),
                lastAnnouncedPitTimingFuelLaps = -1,
                lastPitTimingFuelEvaluationLap = -1,
            )
    }

    private val virtualEnergyFlow =
        narratorFlow
            .telemetry {
                remainingVirtualEnergyUseCases
                    .observeRemainingVirtualEnergy()
                    .onEach {
                        latestVirtualEnergy = it
                        latestVirtualEnergyReceivedAtMs = currentTimeMs()
                    }
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    private val fuelFlow =
        narratorFlow
            .telemetry {
                remainingFuelUseCases.observeFuel().onEach {
                    latestFuel = it
                    latestFuelReceivedAtMs = it.receivedAtMs ?: currentTimeMs()
                }
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    @Suppress("UnusedPrivateProperty")
    private val classCarGapFlow =
        narratorFlow
            .telemetry {
                classCarGapUseCases.observeClassCarGap().onEach {
                    latestClassCarGap = it
                    latestClassCarGapReceivedAtMs = currentTimeMs()
                }
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    @Suppress("UnusedPrivateProperty")
    private val remainingFuelJob =
        fuelFlow
            .onEachCapturingErrors { fuel ->
                val observedAtMs = currentTimeMs()
                val state = narratorState
                val settings = currentSettings
                val decision = narratorUseCases.fuelReadout.determineRemainingFuel(state, fuel, settings)
                narratorState = decision.state
                if (
                    eventProcessor.processRemainingFuel(
                        fuel = fuel,
                        events = decision.events,
                        autoReadoutOrder = autoReadoutOrder.value,
                        queueEnabledStates = queueEnabledStates.value,
                        observedAtMs = observedAtMs,
                        logContext = LmuWindowsTelemetryLogContext(state, settings, decision.state),
                    )
                ) {
                    narratorState = narratorState.copy(remainingFuelWarned = false)
                }
            }.launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val brakeTemperatureJob =
        brakeTemperatureFlow
            .onEachCapturingErrors { brakeTemperature ->
                val observedAtMs = currentTimeMs()
                val state = narratorState
                val settings = currentSettings
                val decision =
                    narratorUseCases.brakeReadout.determineBrakeTemperatureOverheat(
                        state = state,
                        data = brakeTemperature,
                        settings = settings,
                    )
                narratorState = decision.state
                eventProcessor.processBrakeTemperature(
                    brakeTemperature = brakeTemperature,
                    events = decision.events,
                    autoReadoutOrder = autoReadoutOrder.value,
                    queueEnabledStates = queueEnabledStates.value,
                    observedAtMs = observedAtMs,
                    logContext =
                        LmuWindowsTelemetryLogContext(
                            state = state,
                            settings = settings,
                            finalState = decision.state,
                        ),
                )
            }.launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val brakeWearJob =
        brakeWearFlow
            .onEachCapturingErrors { brakeWear ->
                val observedAtMs = currentTimeMs()
                val state = narratorState
                val settings = currentSettings
                val decision =
                    narratorUseCases.brakeReadout.determineBrakeWearLow(
                        state = state,
                        data = brakeWear,
                        settings = settings,
                    )
                narratorState = decision.state
                if (
                    eventProcessor.processBrakeWear(
                        brakeWear = brakeWear,
                        events = decision.events,
                        autoReadoutOrder = autoReadoutOrder.value,
                        queueEnabledStates = queueEnabledStates.value,
                        observedAtMs = observedAtMs,
                        logContext =
                            LmuWindowsTelemetryLogContext(
                                state = state,
                                settings = settings,
                                finalState = decision.state,
                            ),
                    )
                ) {
                    narratorState = narratorState.copy(brakeWearWarned = false)
                }
            }.launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val tyreWearJob =
        tyreWearFlow
            .onEachCapturingErrors { tyreWear ->
                val observedAtMs = currentTimeMs()
                val state = narratorState
                val settings = currentSettings
                val decision =
                    narratorUseCases.tyreReadout.determineTyreWear(
                        state = state,
                        data = tyreWear,
                        settings = settings,
                    )
                narratorState = decision.state
                if (
                    eventProcessor.processTyreWear(
                        tyreWear = tyreWear,
                        events = decision.events,
                        autoReadoutOrder = autoReadoutOrder.value,
                        queueEnabledStates = queueEnabledStates.value,
                        observedAtMs = observedAtMs,
                        logContext =
                            LmuWindowsTelemetryLogContext(
                                state = state,
                                settings = settings,
                                finalState = decision.state,
                            ),
                    )
                ) {
                    narratorState = narratorState.copy(tyreWearWarned = false)
                }
            }.launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val remainingVirtualEnergyJob =
        virtualEnergyFlow
            .onEachCapturingErrors { remainingVirtualEnergy ->
                val observedAtMs = currentTimeMs()
                val state = narratorState
                val settings = currentSettings
                val decision =
                    narratorUseCases.virtualEnergyReadout.determineRemainingVirtualEnergy(
                        state = state,
                        data = remainingVirtualEnergy,
                        settings = settings,
                    )
                narratorState = decision.state
                if (
                    eventProcessor.processRemainingVirtualEnergy(
                        remainingVirtualEnergy = remainingVirtualEnergy,
                        events = decision.events,
                        autoReadoutOrder = autoReadoutOrder.value,
                        queueEnabledStates = queueEnabledStates.value,
                        observedAtMs = observedAtMs,
                        logContext =
                            LmuWindowsTelemetryLogContext(
                                state = state,
                                settings = settings,
                                finalState = decision.state,
                            ),
                    )
                ) {
                    narratorState = narratorState.copy(remainingVirtualEnergyWarned = false)
                }
            }.launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val manualReadoutRequestJob =
        mergeManualReadoutRequests(
            remainingVirtualEnergyUseCases.observeManualReadoutRequests(),
            remainingVirtualEnergyUseCases.observeManualReadoutHotkeyPresses(),
        ).onEachCapturingErrors { request ->
            if (request.simulatorId == Simulator.LmuWindows.id) {
                val set =
                    remainingVirtualEnergyUseCases
                        .observeManualReadoutSets()
                        .first()
                        .find { it.id == request.setId }
                if (set != null) {
                    readManualReadout(set)
                }
            }
        }.launchIn(viewModelScope)

    /** 音声認識の発話から届く項目単位の依頼。1項目だけの一時セットとして既存の手動読み上げに渡す（保存済みセットの編集文言は使わない）。 */
    @Suppress("UnusedPrivateProperty")
    private val manualReadoutItemRequestJob =
        remainingVirtualEnergyUseCases
            .observeManualReadoutItemRequests()
            .onEachCapturingErrors { request ->
                if (request.simulatorId == Simulator.LmuWindows.id) {
                    val spoken =
                        readManualReadout(
                            ManualReadoutSet(id = request.itemKey, itemKeys = listOf(request.itemKey)),
                            playStartSound = request.playStartSound,
                        )
                    // 読み上げに至った場合だけ、音声タブが一致した語句を強調できるよう知らせる
                    if (spoken) remainingVirtualEnergyUseCases.notifyManualReadoutItemSpoken(request)
                }
            }.launchIn(viewModelScope)

    private suspend fun readManualReadout(
        set: ManualReadoutSet,
        playStartSound: Boolean = true,
    ): Boolean {
        if (selectedSimulator.value !is Simulator.LmuWindows) return false
        val observedAtMs = currentTimeMs()
        val items = manualReadoutComposer.compose(set, manualReadoutSnapshot(), narratorState, observedAtMs)
        val state = narratorState
        val settings = currentSettings
        val decision = narratorUseCases.manualReadout.determineManualReadout(state, items)
        narratorState = decision.state
        return eventProcessor.processManualReadout(
            events = decision.events,
            observedAtMs = observedAtMs,
            logContext = LmuWindowsTelemetryLogContext(state, settings, decision.state),
            playStartSound = playStartSound,
        )
    }

    private fun manualReadoutSnapshot(): LmuWindowsManualReadoutSnapshot =
        LmuWindowsManualReadoutSnapshot(
            virtualEnergy = LmuWindowsReceivedValue(latestVirtualEnergy, latestVirtualEnergyReceivedAtMs),
            fuel = LmuWindowsReceivedValue(latestFuel, latestFuelReceivedAtMs),
            classCarGap = LmuWindowsReceivedValue(latestClassCarGap, latestClassCarGapReceivedAtMs),
            tyreWear = LmuWindowsReceivedValue(latestTyreWear, latestTyreWearReceivedAtMs),
            brakeWear = LmuWindowsReceivedValue(latestBrakeWear, latestBrakeWearReceivedAtMs),
            brakeTemperature = LmuWindowsReceivedValue(latestBrakeTemperature, latestBrakeTemperatureReceivedAtMs),
            tyreTemperature = LmuWindowsReceivedValue(latestTyreTemperature, latestTyreTemperatureReceivedAtMs),
            bestLapTimeMs = LmuWindowsReceivedValue(latestBestLapTimeMs, latestBestLapReceivedAtMs),
            pitTimingUpdatedAtMs = latestPitTimingUpdatedAtMs,
        )

    @Suppress("UnusedPrivateProperty")
    private val pitTimingJob =
        narratorFlow
            .pitTiming(
                lmuTelemetryFlow,
                virtualEnergyFlow,
                tyreWearFlow,
                fuelFlow,
            ).onEachCapturingErrors { snapshot ->
                val (telemetry, virtualEnergy, tyreWear, receivedFuel) = snapshot
                val fuel =
                    receivedFuel?.takeIf {
                        it.receivedAtMs?.let { receivedAt ->
                            currentTimeMs() - receivedAt <= NARRATOR_CURRENT_VALUE_MAX_AGE_MS
                        } ?: true
                    }
                val observedAtMs = currentTimeMs()
                latestPitTimingUpdatedAtMs = observedAtMs
                val state = narratorState
                val settings = currentSettings
                val virtualEnergyDecision =
                    virtualEnergy?.let {
                        narratorUseCases.pitTimingReadout.determinePitTimingVirtualEnergy(
                            state = state,
                            telemetry = telemetry,
                            virtualEnergy = virtualEnergy,
                            settings = settings,
                            observedAtMs = observedAtMs,
                        )
                    } ?: LmuWindowsNarratorReadoutDecision(state, emptyList())
                narratorState = virtualEnergyDecision.state
                val tyreWearDecision =
                    tyreWear?.let {
                        narratorUseCases.pitTimingReadout.determinePitTimingTyreWear(
                            state = virtualEnergyDecision.state,
                            telemetry = telemetry,
                            tyreWear = tyreWear,
                            settings = settings,
                            observedAtMs = observedAtMs,
                        )
                    } ?: LmuWindowsNarratorReadoutDecision(virtualEnergyDecision.state, emptyList())
                val fuelDecision =
                    fuel?.let {
                        narratorUseCases.pitTimingReadout.determinePitTimingFuel(
                            state = tyreWearDecision.state,
                            telemetry = telemetry,
                            fuel = fuel,
                            settings = settings,
                            observedAtMs = observedAtMs,
                        )
                    } ?: LmuWindowsNarratorReadoutDecision(tyreWearDecision.state, emptyList())
                val pitTimingEvents =
                    pitTimingLapGate.filter(
                        currentLap = telemetry.timing.currentLap,
                        events =
                            selectLowerPitTimingEvent(
                                virtualEnergyDecision.events,
                                tyreWearDecision.events,
                                fuelDecision.events,
                            ),
                    )
                // 採用された警告だけ履歴へ反映する。優先度・音声再生の成否はこの周回ゲートとは別に扱う。
                narratorState =
                    pitTimingEvents
                        .filterIsInstance<LmuWindowsPitTimingWarning>()
                        .fold(fuelDecision.state) { finalState, event ->
                            narratorUseCases.pitTimingReadout.recordPitTimingAnnounced(finalState, event)
                        }
                eventProcessor.processPitTiming(
                    snapshot =
                        LmuWindowsPitTimingSnapshot(
                            telemetry = telemetry,
                            virtualEnergy = virtualEnergy,
                            tyreWear = tyreWear,
                            fuel = fuel,
                        ),
                    events = pitTimingEvents,
                    autoReadoutOrder = autoReadoutOrder.value,
                    queueEnabledStates = queueEnabledStates.value,
                    observedAtMs = observedAtMs,
                    logContext =
                        LmuWindowsPitTimingLogContext(
                            state = state,
                            settings = settings,
                            finalState = narratorState,
                        ),
                )
            }.launchIn(viewModelScope)

    private val currentSettings: LmuWindowsNarratorReadoutSettings
        get() =
            LmuWindowsNarratorReadoutSettings(
                enabledStates = mergedEnabledStates.value,
                currentLap = currentLap.value,
                skipFirstLap = skipFirstLap.value,
                vehicleApproachSustainedApproachDurationSeconds = sustainedApproachDurationSeconds.value,
                tyreTemperatureHighThresholdCelsius = tyreHighThreshold.value,
                tyreTemperatureLowWarningPhases = tyreLowWarningPhases.value,
                tyreWearThresholdPercentage = tyreWearThresholdPercentage.value,
                brakeTemperatureHighThresholdCelsius = brakeTemperatureHighThresholdCelsius.value,
                brakeWearLowThresholdPercent = brakeWearLowThresholdPercent.value,
                remainingVirtualEnergyThresholdPercentage = remainingVirtualEnergyThresholdPercentage.value,
                remainingFuelThresholdPercentage = remainingFuelThresholdPercentage.value,
                energySource = energySourceFlow.value,
                pitTimingVirtualEnergyLapsThreshold = pitTimingVirtualEnergyLapsThreshold.value,
                pitTimingTyreWearLapsThreshold = pitTimingTyreWearLapsThreshold.value,
                pitTimingFuelLapsThreshold = pitTimingFuelLapsThreshold.value,
            )
}

/**
 * バーチャルエネルギー・タイヤ摩耗・燃料のピットタイミング警告が同一tickで発火した場合、
 * 予想残り周回数が低い方（＝ピット作業がより緊急な方）のみを1回読み上げる。
 */
internal fun selectLowerPitTimingEvent(
    virtualEnergyEvents: List<SpeechEvent>,
    tyreWearEvents: List<SpeechEvent>,
    fuelEvents: List<SpeechEvent>,
): List<LmuWindowsPitTimingWarning> =
    listOfNotNull(
        (virtualEnergyEvents + tyreWearEvents + fuelEvents)
            .filterIsInstance<LmuWindowsPitTimingWarning>()
            .minByOrNull { it.laps },
    )
