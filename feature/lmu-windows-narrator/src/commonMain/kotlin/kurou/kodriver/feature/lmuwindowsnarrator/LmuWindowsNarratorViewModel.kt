package kurou.kodriver.feature.lmuwindowsnarrator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.engine.LmuWindowsPitTimingWarning
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_APPROACH_SKIP_FIRST_LAP_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_CLASS_UNKNOWN_KEY
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemKeys
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemTextDefault
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsTyreTemperatureLowWarningDefaultPhases
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault
import kurou.kodriver.domain.lmuwindows.model.resolveLmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsius
import kurou.kodriver.domain.lmuwindows.model.resolveLmuWindowsVehicleClassBrakeWearLowThresholdPercent
import kurou.kodriver.domain.lmuwindows.model.resolveLmuWindowsVehicleClassTyreTemperatureHighThresholdCelsius
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsBrakeReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsManualReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsMyBestLapReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsPitTimingReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsRaceFlagReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsTyreReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsVehicleReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsVirtualEnergyReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.LmuWindowsNarratorReadoutSettings
import kurou.kodriver.domain.lmuwindows.usecase.LmuWindowsNarratorState
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeTemperatureUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeWearRemainingUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreDetachedUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreWearThresholdPercentageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreWearUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachSustainedDurationUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import kurou.kodriver.domain.lmuwindows.usecase.TyreTemperatureReadoutInput
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.LmuWindowsManualReadoutItem
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SELECTED_SIMULATOR_DEFAULT
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.isLmuWindowsManualReadoutTemperatureItem
import kurou.kodriver.domain.usecase.ObserveAutoReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveDebugReadoutRequestUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutHotkeyPressesUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutRequestsUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutSetsUseCase
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutHotkeyUseCase
import kurou.kodriver.domain.usecase.ObserveResolvedAutoReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal data class VehicleApproachUseCases(
    val observeVehicleApproach: ObserveLmuWindowsVehicleApproachUseCase,
    val observeLmuWindows: ObserveLmuWindowsUseCase,
    val observeSkipFirstLap: ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase,
    val observeEnabledStates: ObserveLmuWindowsVehicleApproachEnabledStatesUseCase,
    val observeSustainedApproachDuration: ObserveLmuWindowsVehicleApproachSustainedDurationUseCase,
)

internal data class VehicleDamageUseCases(
    val observeVehicleDamage: ObserveLmuWindowsVehicleDamageUseCase,
    val observeVehicleDamageEnabledStates: ObserveLmuWindowsVehicleDamageEnabledStatesUseCase,
)

internal data class TyreDetachedUseCases(
    val observeTyreDetached: ObserveLmuWindowsTyreDetachedUseCase,
)

internal data class SimulatorUseCases(
    val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
)

internal data class ReadoutListUseCases(
    val observeAutoReadoutEnabledStates: ObserveAutoReadoutEnabledStatesUseCase,
    val observeAutoReadoutOrder: ObserveResolvedAutoReadoutOrderUseCase,
    val observeQueueEnabledStates: ObserveQueueEnabledStatesUseCase,
)

internal data class FlagUseCases(
    val observeRaceFlags: ObserveLmuWindowsRaceFlagsUseCase,
    val observeFlagEnabledStates: ObserveLmuWindowsFlagEnabledStatesUseCase,
)

internal data class TyreTemperatureUseCases(
    val observeTyreCarcassTemperature: ObserveLmuWindowsTyreCarcassTemperatureUseCase,
    val observeVehicleClassHighThreshold: ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase,
    val observeVehicleClass: ObserveLmuWindowsVehicleClassUseCase,
    val observeTyreTemperatureEnabledStates: ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase,
    val observeLowWarningPhases: ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase,
)

internal data class TyreWearUseCases(
    val observeTyreWear: ObserveLmuWindowsTyreWearUseCase,
    val observeThresholdPercentage: ObserveLmuWindowsTyreWearThresholdPercentageUseCase,
)

internal data class RemainingVirtualEnergyUseCases(
    val observeRemainingVirtualEnergy: ObserveLmuWindowsVirtualEnergyUseCase,
    val observeReadoutHotkey: ObserveReadoutHotkeyUseCase,
    val observeManualReadoutHotkeyPresses: ObserveManualReadoutHotkeyPressesUseCase,
    val observeManualReadoutRequests: ObserveManualReadoutRequestsUseCase,
    val observeManualReadoutSets: ObserveManualReadoutSetsUseCase,
    val observeThresholdPercentage: ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase,
)

internal data class BrakeTemperatureUseCases(
    val observeBrakeTemperature: ObserveLmuWindowsBrakeTemperatureUseCase,
    val observeVehicleClassHighThreshold: ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase,
)

internal data class BrakeWearUseCases(
    val observeBrakeWear: ObserveLmuWindowsBrakeWearRemainingUseCase,
    val observeVehicleClassLowThreshold: ObserveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase,
)

internal data class PitTimingUseCases(
    val observeVirtualEnergyLapsThreshold: ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase,
    val observeTyreWearLapsThreshold: ObserveLmuWindowsPitTimingTyreWearLapsUseCase,
    val observeEnabledStates: ObserveLmuWindowsPitTimingEnabledStatesUseCase,
)

internal data class NarratorUseCases(
    val myBestLapReadout: DetermineLmuWindowsMyBestLapReadoutUseCase,
    val vehicleReadout: DetermineLmuWindowsVehicleReadoutUseCase,
    val tyreReadout: DetermineLmuWindowsTyreReadoutUseCase,
    val brakeReadout: DetermineLmuWindowsBrakeReadoutUseCase,
    val virtualEnergyReadout: DetermineLmuWindowsVirtualEnergyReadoutUseCase,
    val manualReadout: DetermineLmuWindowsManualReadoutUseCase,
    val pitTimingReadout: DetermineLmuWindowsPitTimingReadoutUseCase,
    val raceFlagReadout: DetermineLmuWindowsRaceFlagReadoutUseCase,
)

// 共有メモリは短周期で届くため、これを超えて更新がなければ切断・セッション終了とみなす。
private const val CURRENT_VALUE_MAX_AGE_MS = 5_000L
private const val PERCENTAGE_SCALE = 100.0

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
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
    pitTimingUseCases: PitTimingUseCases,
    private val eventProcessor: LmuWindowsNarratorEventProcessor,
    private val narratorUseCases: NarratorUseCases,
    observeDebugReadoutRequest: ObserveDebugReadoutRequestUseCase,
    private val currentTimeMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : ViewModel() {
    private var narratorState = LmuWindowsNarratorState()

    // ピットタイミング警告は同一ラップ内で予想残り周回数が最も低いものだけを読み上げる（別tickの後着もより緊急なら読み上げる）。
    private val pitTimingLapGate = PitTimingLapGate()

    private val selectedSimulator =
        simulatorUseCases
            .observeSelectedSimulator()
            .stateIn(viewModelScope, SharingStarted.Eagerly, SELECTED_SIMULATOR_DEFAULT)

    // listPane（readoutStates）とdetailPane（flagStates・vehicleDamageStates）を統合した、
    // Narratorの読み上げ判定に実際に使う唯一のenabledStates。
    private val mergedEnabledStates =
        combine(
            listOf(
                selectedSimulator
                    .flatMapLatest { simulator ->
                        readoutListUseCases.observeAutoReadoutEnabledStates(simulator.id)
                    },
                flagUseCases.observeFlagEnabledStates(),
                vehicleDamageUseCases.observeVehicleDamageEnabledStates(),
                tyreTemperatureUseCases.observeTyreTemperatureEnabledStates(),
                vehicleApproachUseCases.observeEnabledStates(),
                pitTimingUseCases.observeEnabledStates(),
            ),
        ) { states: Array<Map<ReadoutItemKey, Boolean>> ->
            states.fold(emptyMap<ReadoutItemKey, Boolean>()) { acc, enabledStates -> acc + enabledStates }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap<ReadoutItemKey, Boolean>())

    // index が小さいほど優先度が高い（リスト上位 = 高優先）
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

    private var latestBestLapTimeMs: Long? = null
    private var latestBestLapReceivedAtMs: Long = 0L
    private var latestPitTimingUpdatedAtMs: Long = 0L

    private val lmuTelemetryFlow =
        selectedSimulator
            .flatMapLatest { simulator ->
                latestBestLapTimeMs = null
                if (simulator !is Simulator.LmuWindows) {
                    emptyFlow()
                } else {
                    vehicleApproachUseCases.observeLmuWindows().onEach {
                        latestBestLapTimeMs = it.timing.bestLapTimeMs
                        latestBestLapReceivedAtMs = currentTimeMs()
                    }
                }
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    private val raceFlagsFlow =
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
                flagUseCases.observeRaceFlags()
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    private val currentLap =
        lmuTelemetryFlow
            .map { it.timing.currentLap }
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val vehicleClassFlow =
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
                tyreTemperatureUseCases.observeVehicleClass()
            }

    private val tyreHighThreshold =
        combine(
            vehicleClassFlow,
            tyreTemperatureUseCases.observeVehicleClassHighThreshold(),
        ) { vehicleClass, thresholdsByVehicleClass ->
            resolveLmuWindowsVehicleClassTyreTemperatureHighThresholdCelsius(thresholdsByVehicleClass, vehicleClass)
        }.stateIn(
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
        combine(
            vehicleClassFlow,
            brakeTemperatureUseCases.observeVehicleClassHighThreshold(),
        ) { vehicleClass, thresholdsByVehicleClass ->
            Celsius(
                resolveLmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsius(
                    thresholdsByVehicleClass,
                    vehicleClass,
                ),
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            Celsius(
                lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(
                    LmuWindowsVehicleClassData.Unknown(LMU_WINDOWS_VEHICLE_CLASS_UNKNOWN_KEY),
                ),
            ),
        )

    private val brakeWearLowThresholdPercent =
        combine(
            vehicleClassFlow,
            brakeWearUseCases.observeVehicleClassLowThreshold(),
        ) { vehicleClass, thresholdsByVehicleClass ->
            resolveLmuWindowsVehicleClassBrakeWearLowThresholdPercent(thresholdsByVehicleClass, vehicleClass)
        }.stateIn(
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

    private val pitTimingVirtualEnergyLapsThreshold =
        pitTimingUseCases
            .observeVirtualEnergyLapsThreshold()
            .stateIn(viewModelScope, SharingStarted.Eagerly, LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT)

    private val pitTimingTyreWearLapsThreshold =
        pitTimingUseCases
            .observeTyreWearLapsThreshold()
            .stateIn(viewModelScope, SharingStarted.Eagerly, LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT)

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
            .onEach { request ->
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
            .onEach { telemetry ->
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
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
                vehicleApproachUseCases.observeVehicleApproach()
            }.onEach { vehicleApproach ->
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
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
                vehicleDamageUseCases.observeVehicleDamage()
            }.onEach { vehicleDamage ->
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
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
                tyreDetachedUseCases.observeTyreDetached()
            }.onEach { tyreDetached ->
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
            .onEach { raceFlags ->
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
        selectedSimulator
            .flatMapLatest { simulator ->
                latestTyreTemperature = null
                if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
                combine(
                    tyreTemperatureUseCases
                        .observeTyreCarcassTemperature()
                        .onEach {
                            latestTyreTemperature = it
                            latestTyreTemperatureReceivedAtMs = currentTimeMs()
                        },
                    raceFlagsFlow,
                ) { tyreCarcassTemperature, raceFlags -> tyreCarcassTemperature to raceFlags }
            }.onEach { (tyreCarcassTemperature, raceFlags) ->
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
        selectedSimulator
            .flatMapLatest { simulator ->
                latestTyreWear = null
                if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
                tyreWearUseCases.observeTyreWear().onEach {
                    latestTyreWear = it
                    latestTyreWearReceivedAtMs = currentTimeMs()
                }
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    private var latestBrakeTemperature: LmuWindowsBrakeTemperatureData? = null
    private var latestBrakeTemperatureReceivedAtMs: Long = 0L

    private val brakeTemperatureFlow =
        selectedSimulator
            .flatMapLatest { simulator ->
                latestBrakeTemperature = null
                if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
                brakeTemperatureUseCases.observeBrakeTemperature().onEach {
                    latestBrakeTemperature = it
                    latestBrakeTemperatureReceivedAtMs = currentTimeMs()
                }
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    private var latestBrakeWear: LmuWindowsBrakeWearRemainingData? = null
    private var latestBrakeWearReceivedAtMs: Long = 0L

    private val brakeWearFlow =
        selectedSimulator
            .flatMapLatest { simulator ->
                latestBrakeWear = null
                if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
                brakeWearUseCases
                    .observeBrakeWear()
                    .onEach {
                        latestBrakeWear = it
                        latestBrakeWearReceivedAtMs = currentTimeMs()
                    }.filterNotNull()
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    // replay なしの共有 Flow を再購読せず、共有前に最新値と受信時刻を保持する。切り替え時は破棄する。
    private var latestVirtualEnergy: LmuWindowsVirtualEnergyData? = null
    private var latestVirtualEnergyReceivedAtMs: Long = 0L

    private val virtualEnergyFlow =
        selectedSimulator
            .flatMapLatest { simulator ->
                latestVirtualEnergy = null
                if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
                remainingVirtualEnergyUseCases
                    .observeRemainingVirtualEnergy()
                    .onEach {
                        latestVirtualEnergy = it
                        latestVirtualEnergyReceivedAtMs = currentTimeMs()
                    }
            }.shareIn(viewModelScope, SharingStarted.Eagerly)

    @Suppress("UnusedPrivateProperty")
    private val brakeTemperatureJob =
        brakeTemperatureFlow
            .onEach { brakeTemperature ->
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
            .onEach { brakeWear ->
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
            }.launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val tyreWearJob =
        tyreWearFlow
            .onEach { tyreWear ->
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
            }.launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val remainingVirtualEnergyJob =
        virtualEnergyFlow
            .onEach { remainingVirtualEnergy ->
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
            }.launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val readoutHotkeyJob =
        remainingVirtualEnergyUseCases
            .observeReadoutHotkey()
            .onEach {
                readRemainingVirtualEnergyCurrentValue()
            }.launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val manualReadoutRequestJob =
        merge(
            remainingVirtualEnergyUseCases.observeManualReadoutRequests(),
            remainingVirtualEnergyUseCases.observeManualReadoutHotkeyPresses(),
        ).onEach { request ->
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

    private suspend fun readManualReadout(set: ManualReadoutSet) {
        if (selectedSimulator.value !is Simulator.LmuWindows) return
        val observedAtMs = currentTimeMs()
        val energy =
            latestVirtualEnergy?.takeIf {
                observedAtMs - latestVirtualEnergyReceivedAtMs <= CURRENT_VALUE_MAX_AGE_MS
            }
        val tyreWear =
            latestTyreWear?.takeIf {
                observedAtMs - latestTyreWearReceivedAtMs <= CURRENT_VALUE_MAX_AGE_MS
            }
        val brakeWear =
            latestBrakeWear?.takeIf {
                observedAtMs - latestBrakeWearReceivedAtMs <= CURRENT_VALUE_MAX_AGE_MS
            }
        val brakeTemperature =
            latestBrakeTemperature?.takeIf {
                observedAtMs - latestBrakeTemperatureReceivedAtMs <= CURRENT_VALUE_MAX_AGE_MS
            }
        val tyreTemperature =
            latestTyreTemperature?.takeIf {
                observedAtMs - latestTyreTemperatureReceivedAtMs <= CURRENT_VALUE_MAX_AGE_MS
            }
        val bestLapTimeMs =
            latestBestLapTimeMs?.takeIf {
                observedAtMs - latestBestLapReceivedAtMs <= CURRENT_VALUE_MAX_AGE_MS && it > 0
            }
        val items =
            set.itemKeys.filter { it in lmuWindowsManualReadoutItemKeys() }.map { key ->
                val ratio =
                    if (key == LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value ||
                        key == LmuWindowsReadoutItemKey.PitTiming.TyreWear.value ||
                        key == LmuWindowsReadoutItemKey.MyBestLap.Root.value ||
                        isLmuWindowsManualReadoutTemperatureItem(key)
                    ) {
                        null
                    } else if (key == LmuWindowsReadoutItemKey.BrakeWear.Root.value) {
                        brakeWear?.let { data ->
                            data.wheels.values
                                .minOfOrNull { it.remainingPercent }
                                ?.let { it / PERCENTAGE_SCALE }
                        }
                    } else if (key == LmuWindowsReadoutItemKey.TyreWear.Root.value) {
                        tyreWear
                            ?.wheels
                            ?.values
                            ?.minOrNull()
                            ?.value
                    } else {
                        energy?.remainingRatio?.value
                    }
                val celsius =
                    manualReadoutCelsius(key, brakeTemperature?.wheels?.values, tyreTemperature?.wheels?.values)
                LmuWindowsManualReadoutItem(
                    key,
                    set.itemTexts[key] ?: lmuWindowsManualReadoutItemTextDefault(key),
                    ratio,
                    celsius,
                    lapTimeMs = bestLapTimeMs.takeIf { key == LmuWindowsReadoutItemKey.MyBestLap.Root.value },
                    remainingLaps =
                        manualRemainingLaps(
                            key = key,
                            hasEnergy = energy != null,
                            hasTyreWear = tyreWear != null,
                            pitTimingFresh = observedAtMs - latestPitTimingUpdatedAtMs <= CURRENT_VALUE_MAX_AGE_MS,
                        ),
                )
            }
        val state = narratorState
        val settings = currentSettings
        val decision = narratorUseCases.manualReadout.determineManualReadout(state, items)
        narratorState = decision.state
        eventProcessor.processManualReadout(
            events = decision.events,
            autoReadoutOrder = autoReadoutOrder.value,
            queueEnabledStates = queueEnabledStates.value + (decision.events.first().readoutItemKey to true),
            observedAtMs = observedAtMs,
            logContext = LmuWindowsTelemetryLogContext(state, settings, decision.state),
        )
    }

    private fun manualRemainingLaps(
        key: String,
        hasEnergy: Boolean,
        hasTyreWear: Boolean,
        pitTimingFresh: Boolean,
    ): Int? =
        when {
            !pitTimingFresh -> {
                null
            }

            key == LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value && hasEnergy -> {
                narratorUseCases.manualReadout.manualPitTimingVirtualEnergyRemainingLaps(narratorState)
            }

            key == LmuWindowsReadoutItemKey.PitTiming.TyreWear.value && hasTyreWear -> {
                narratorUseCases.manualReadout.manualPitTimingTyreWearRemainingLaps(narratorState)
            }

            else -> {
                null
            }
        }

    private suspend fun readRemainingVirtualEnergyCurrentValue() {
        if (selectedSimulator.value !is Simulator.LmuWindows) return
        val observedAtMs = currentTimeMs()
        val remainingVirtualEnergy =
            latestVirtualEnergy?.takeIf {
                observedAtMs - latestVirtualEnergyReceivedAtMs <= CURRENT_VALUE_MAX_AGE_MS
            } ?: return
        val state = narratorState
        val settings = currentSettings
        val decision =
            narratorUseCases.virtualEnergyReadout.determineRemainingVirtualEnergyCurrentValue(
                state = state,
                data = remainingVirtualEnergy,
                settings = settings,
            )
        narratorState = decision.state
        eventProcessor.processRemainingVirtualEnergy(
            remainingVirtualEnergy = remainingVirtualEnergy,
            events = decision.events,
            autoReadoutOrder = autoReadoutOrder.value,
            queueEnabledStates = queueEnabledStates.value,
            observedAtMs = observedAtMs,
            logContext = LmuWindowsTelemetryLogContext(state, settings, decision.state),
        )
    }

    @Suppress("UnusedPrivateProperty")
    private val pitTimingJob =
        combine(
            lmuTelemetryFlow,
            virtualEnergyFlow,
            tyreWearFlow,
        ) { telemetry, virtualEnergy, tyreWear -> Triple(telemetry, virtualEnergy, tyreWear) }
            .onEach { (telemetry, virtualEnergy, tyreWear) ->
                val observedAtMs = currentTimeMs()
                latestPitTimingUpdatedAtMs = observedAtMs
                val state = narratorState
                val settings = currentSettings
                val virtualEnergyDecision =
                    narratorUseCases.pitTimingReadout.determinePitTimingVirtualEnergy(
                        state = state,
                        telemetry = telemetry,
                        virtualEnergy = virtualEnergy,
                        settings = settings,
                        observedAtMs = observedAtMs,
                    )
                narratorState = virtualEnergyDecision.state
                val tyreWearDecision =
                    narratorUseCases.pitTimingReadout.determinePitTimingTyreWear(
                        state = virtualEnergyDecision.state,
                        telemetry = telemetry,
                        tyreWear = tyreWear,
                        settings = settings,
                        observedAtMs = observedAtMs,
                    )
                val pitTimingEvents =
                    pitTimingLapGate.filter(
                        currentLap = telemetry.timing.currentLap,
                        events = selectLowerPitTimingEvent(virtualEnergyDecision.events, tyreWearDecision.events),
                    )
                // 採用された警告だけ履歴へ反映する。優先度・音声再生の成否はこの周回ゲートとは別に扱う。
                narratorState =
                    pitTimingEvents
                        .filterIsInstance<LmuWindowsPitTimingWarning>()
                        .fold(tyreWearDecision.state) { finalState, event ->
                            narratorUseCases.pitTimingReadout.recordPitTimingAnnounced(finalState, event)
                        }
                eventProcessor.processPitTiming(
                    snapshot =
                        LmuWindowsPitTimingSnapshot(
                            telemetry = telemetry,
                            virtualEnergy = virtualEnergy,
                            tyreWear = tyreWear,
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
                pitTimingVirtualEnergyLapsThreshold = pitTimingVirtualEnergyLapsThreshold.value,
                pitTimingTyreWearLapsThreshold = pitTimingTyreWearLapsThreshold.value,
            )
}

/** 温度系の手動読み上げ項目の最高温度。温度系以外、またはデータ無しなら null。 */
private fun manualReadoutCelsius(
    key: String,
    brakeReadings: Collection<CelsiusReading>?,
    tyreReadings: Collection<CelsiusReading>?,
): Float? =
    when (key) {
        LmuWindowsReadoutItemKey.BrakeTemperature.Root.value -> brakeReadings?.let { maxFiniteCelsius(it) }
        LmuWindowsReadoutItemKey.TyreTemperature.Root.value -> tyreReadings?.let { maxFiniteCelsius(it) }
        else -> null
    }

/** 非有限値（NaN・無限大）を除いた最高温度。有効な値が無ければ null（データ無し扱い）。 */
private fun maxFiniteCelsius(readings: Collection<CelsiusReading>): Float? =
    readings
        .map { it.value }
        .filter { it.isFinite() }
        .maxOrNull()

/**
 * バーチャルエネルギー・タイヤ摩耗のピットタイミング警告が同一tickで両方発火した場合、
 * 予想残り周回数が低い方（＝ピット作業がより緊急な方）のみを1回読み上げる。
 */
private fun selectLowerPitTimingEvent(
    virtualEnergyEvents: List<SpeechEvent>,
    tyreWearEvents: List<SpeechEvent>,
): List<LmuWindowsPitTimingWarning> {
    val virtualEnergyEvent =
        virtualEnergyEvents.filterIsInstance<LmuWindowsPitTimingWarning>().firstOrNull()
    val tyreWearEvent = tyreWearEvents.filterIsInstance<LmuWindowsPitTimingWarning>().firstOrNull()
    return when {
        virtualEnergyEvent != null && tyreWearEvent != null -> {
            if (virtualEnergyEvent.laps <= tyreWearEvent.laps) listOf(virtualEnergyEvent) else listOf(tyreWearEvent)
        }

        virtualEnergyEvent != null -> {
            listOf(virtualEnergyEvent)
        }

        tyreWearEvent != null -> {
            listOf(tyreWearEvent)
        }

        else -> {
            emptyList()
        }
    }
}
