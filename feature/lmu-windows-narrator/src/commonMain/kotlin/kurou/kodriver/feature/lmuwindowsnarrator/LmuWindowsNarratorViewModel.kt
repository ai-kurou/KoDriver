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
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.RedFlagVoiceType
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.model.VehicleApproachSustainedReadoutType
import kurou.kodriver.domain.model.lmuWindowsTyreTemperatureLowWarningDefaultPhases
import kurou.kodriver.domain.usecase.DetermineLmuWindowsNarratorReadoutUseCase
import kurou.kodriver.domain.usecase.LmuWindowsNarratorReadoutSettings
import kurou.kodriver.domain.usecase.LmuWindowsNarratorState
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRemainingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartReadoutTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedDurationUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedReadoutTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

internal data class VehicleApproachUseCases(
    val observeVehicleApproach: ObserveLmuWindowsVehicleApproachUseCase,
    val observeLmuWindows: ObserveLmuWindowsUseCase,
    val observeSkipFirstLap: ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase,
    val observeEnabledStates: ObserveLmuWindowsVehicleApproachEnabledStatesUseCase,
    val observeStartReadoutType: ObserveLmuWindowsVehicleApproachStartReadoutTypeUseCase,
    val observeSustainedApproachDuration: ObserveLmuWindowsVehicleApproachSustainedDurationUseCase,
    val observeSustainedReadoutType: ObserveLmuWindowsVehicleApproachSustainedReadoutTypeUseCase,
)

internal data class VehicleDamageUseCases(
    val observeVehicleDamage: ObserveLmuWindowsVehicleDamageUseCase,
    val observeVehicleDamageEnabledStates: ObserveLmuWindowsVehicleDamageEnabledStatesUseCase,
)

internal data class ReadoutListUseCases(
    val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    val observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    val observeReadoutOrder: ObserveReadoutOrderUseCase,
    val observeQueueEnabledStates: ObserveQueueEnabledStatesUseCase,
)

internal data class FlagUseCases(
    val observeRaceFlags: ObserveLmuWindowsRaceFlagsUseCase,
    val observeFlagEnabledStates: ObserveLmuWindowsFlagEnabledStatesUseCase,
)

internal data class TyreTemperatureUseCases(
    val observeTyreCarcassTemperature: ObserveLmuWindowsTyreCarcassTemperatureUseCase,
    val observeHighThreshold: ObserveLmuWindowsTyreTemperatureHighThresholdUseCase,
    val observeTyreTemperatureEnabledStates: ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase,
    val observeLowWarningPhases: ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase,
)

internal data class TyreWearUseCases(
    val observeTyreWear: ObserveLmuWindowsTyreWearUseCase,
    val observeThresholdPercentage: ObserveLmuWindowsTyreWearThresholdPercentageUseCase,
)

internal data class NarratorUseCases(
    val determineReadout: DetermineLmuWindowsNarratorReadoutUseCase,
    val observeMyBestLapVoiceType: ObserveLmuWindowsMyBestLapVoiceTypeUseCase,
    val observeRedFlagVoiceType: ObserveLmuWindowsRedFlagVoiceTypeUseCase,
    val observeVirtualEnergy: ObserveLmuWindowsVirtualEnergyUseCase,
    val observeRemainingVirtualEnergyLapsThreshold: ObserveLmuWindowsRemainingVirtualEnergyLapsUseCase,
)

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("LongParameterList")
internal class LmuWindowsNarratorViewModel(
    vehicleApproachUseCases: VehicleApproachUseCases,
    vehicleDamageUseCases: VehicleDamageUseCases,
    readoutListUseCases: ReadoutListUseCases,
    flagUseCases: FlagUseCases,
    tyreTemperatureUseCases: TyreTemperatureUseCases,
    tyreWearUseCases: TyreWearUseCases,
    private val eventProcessor: LmuWindowsNarratorEventProcessor,
    private val narratorUseCases: NarratorUseCases,
    private val currentTimeMs: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {

    private var narratorState = LmuWindowsNarratorState()

    private val selectedSimulator = readoutListUseCases.observeSelectedSimulator()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // listPane（readoutStates）とdetailPane（flagStates・vehicleDamageStates）を統合した、
    // Narratorの読み上げ判定に実際に使う唯一のenabledStates。
    private val mergedEnabledStates = combine(
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator == null) emptyFlow<Map<ReadoutItemKey, Boolean>>()
                else readoutListUseCases.observeReadoutEnabledStates(simulator.id)
            },
        flagUseCases.observeFlagEnabledStates(),
        vehicleDamageUseCases.observeVehicleDamageEnabledStates(),
        tyreTemperatureUseCases.observeTyreTemperatureEnabledStates(),
        vehicleApproachUseCases.observeEnabledStates(),
    ) {
            readoutStates: Map<ReadoutItemKey, Boolean>,
            flagStates,
            vehicleDamageStates,
            tyreTemperatureStates,
            vehicleApproachStates,
        ->
        readoutStates + flagStates + vehicleDamageStates + tyreTemperatureStates + vehicleApproachStates
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap<ReadoutItemKey, Boolean>())

    // index が小さいほど優先度が高い（リスト上位 = 高優先）
    private val readoutOrder = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator == null) emptyFlow() else readoutListUseCases.observeReadoutOrder(simulator.id)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // キューに追加して読み上げるかどうか（ReadoutItemKey.TopLevel 単位）。
    private val queueEnabledStates = readoutListUseCases.observeQueueEnabledStates()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap<ReadoutItemKey, Boolean>())

    private val lmuTelemetryFlow = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator !is Simulator.LmuWindows) emptyFlow()
            else vehicleApproachUseCases.observeLmuWindows()
        }
        .shareIn(viewModelScope, SharingStarted.Eagerly)

    private val raceFlagsFlow = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
            flagUseCases.observeRaceFlags()
        }
        .shareIn(viewModelScope, SharingStarted.Eagerly)

    private val currentLap = lmuTelemetryFlow
        .map { it.timing.currentLap }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val voiceType = narratorUseCases.observeMyBestLapVoiceType()
        .stateIn(viewModelScope, SharingStarted.Eagerly, MyBestLapVoiceType.FORMAL)

    private val redFlagVoiceType = narratorUseCases.observeRedFlagVoiceType()
        .stateIn(viewModelScope, SharingStarted.Eagerly, RedFlagVoiceType.SESSION_STOP)

    private val tyreHighThreshold = tyreTemperatureUseCases.observeHighThreshold()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 95)

    private val tyreLowWarningPhases = tyreTemperatureUseCases.observeLowWarningPhases()
        .stateIn(viewModelScope, SharingStarted.Eagerly, lmuWindowsTyreTemperatureLowWarningDefaultPhases)

    private val tyreWearThresholdPercentage = tyreWearUseCases.observeThresholdPercentage()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 50)

    private val skipFirstLap = vehicleApproachUseCases.observeSkipFirstLap()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val startReadoutType = vehicleApproachUseCases.observeStartReadoutType()
        .stateIn(viewModelScope, SharingStarted.Eagerly, VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)

    private val sustainedApproachDurationSeconds = vehicleApproachUseCases.observeSustainedApproachDuration()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT,
        )

    private val sustainedReadoutType = vehicleApproachUseCases.observeSustainedReadoutType()
        .stateIn(viewModelScope, SharingStarted.Eagerly, VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT)

    private val remainingVirtualEnergyLapsThreshold = narratorUseCases.observeRemainingVirtualEnergyLapsThreshold()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 3)

    private val virtualEnergyFlow = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator !is Simulator.LmuWindows) emptyFlow()
            else narratorUseCases.observeVirtualEnergy()
        }
        .shareIn(viewModelScope, SharingStarted.Eagerly)

    @Suppress("UnusedPrivateProperty")
    private val myBestLapJob = lmuTelemetryFlow
        .onEach { telemetry ->
            val observedAtMs = currentTimeMs()
            val state = narratorState
            val settings = currentSettings
            val decision = narratorUseCases.determineReadout.determineMyBestLap(
                state = state,
                telemetry = telemetry,
                settings = settings,
            )
            narratorState = decision.state
            eventProcessor.processTelemetry(
                telemetry = telemetry,
                events = decision.events,
                readoutOrder = readoutOrder.value,
                queueEnabledStates = queueEnabledStates.value,
                observedAtMs = observedAtMs,
                logContext = LmuWindowsTelemetryLogContext(
                    state = state,
                    settings = settings,
                    finalState = decision.state,
                ),
            )
        }
        .launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val vehicleApproachJob = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
            vehicleApproachUseCases.observeVehicleApproach()
        }
        .onEach { vehicleApproach ->
            val observedAtMs = currentTimeMs()
            val state = narratorState
            val settings = currentSettings
            val decision = narratorUseCases.determineReadout.determineVehicleApproach(
                state = state,
                vehicleApproach = vehicleApproach,
                settings = settings,
                observedAtMs = observedAtMs,
            )
            narratorState = decision.state
            eventProcessor.processVehicleApproach(
                vehicleApproach = vehicleApproach,
                events = decision.events,
                readoutOrder = readoutOrder.value,
                queueEnabledStates = queueEnabledStates.value,
                observedAtMs = observedAtMs,
                logContext = LmuWindowsTelemetryLogContext(
                    state = state,
                    settings = settings,
                    finalState = decision.state,
                ),
            )
        }
        .launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val vehicleDamageJob = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
            vehicleDamageUseCases.observeVehicleDamage()
        }
        .onEach { vehicleDamage ->
            val observedAtMs = currentTimeMs()
            val state = narratorState
            val settings = currentSettings
            val decision = narratorUseCases.determineReadout.determineVehicleDamage(
                state = state,
                vehicleDamage = vehicleDamage,
                settings = settings,
            )
            narratorState = decision.state
            eventProcessor.processVehicleDamage(
                vehicleDamage = vehicleDamage,
                events = decision.events,
                readoutOrder = readoutOrder.value,
                queueEnabledStates = queueEnabledStates.value,
                observedAtMs = observedAtMs,
                logContext = LmuWindowsTelemetryLogContext(
                    state = state,
                    settings = settings,
                    finalState = decision.state,
                ),
            )
        }
        .launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val flagJob = raceFlagsFlow
        .onEach { raceFlags ->
            val observedAtMs = currentTimeMs()
            val state = narratorState
            val settings = currentSettings
            val decision = narratorUseCases.determineReadout.determineRaceFlags(
                state = state,
                raceFlags = raceFlags,
                settings = settings,
            )
            narratorState = decision.state
            eventProcessor.processRaceFlags(
                raceFlags = raceFlags,
                events = decision.events,
                readoutOrder = readoutOrder.value,
                queueEnabledStates = queueEnabledStates.value,
                observedAtMs = observedAtMs,
                logContext = LmuWindowsTelemetryLogContext(
                    state = state,
                    settings = settings,
                    finalState = decision.state,
                ),
            )
        }
        .launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val tyreTemperatureJob = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
            combine(
                tyreTemperatureUseCases.observeTyreCarcassTemperature(),
                raceFlagsFlow,
            ) { tyreCarcassTemperature, raceFlags -> tyreCarcassTemperature to raceFlags }
        }
        .onEach { (tyreCarcassTemperature, raceFlags) ->
            val observedAtMs = currentTimeMs()
            val state = narratorState
            val settings = currentSettings
            val overheatDecision = narratorUseCases.determineReadout.determineTyreTemperatureOverheat(
                state = state,
                data = tyreCarcassTemperature,
                settings = settings,
            )
            narratorState = overheatDecision.state
            val lowDecision = narratorUseCases.determineReadout.determineTyreTemperatureLow(
                state = overheatDecision.state,
                data = tyreCarcassTemperature,
                raceFlags = raceFlags,
                settings = settings,
            )
            narratorState = lowDecision.state
            eventProcessor.processTyreTemperature(
                tyreCarcassTemperature = tyreCarcassTemperature,
                raceFlags = raceFlags,
                events = overheatDecision.events + lowDecision.events,
                readoutOrder = readoutOrder.value,
                queueEnabledStates = queueEnabledStates.value,
                observedAtMs = observedAtMs,
                logContext = LmuWindowsTyreTemperatureLogContext(
                    state = state,
                    settings = settings,
                    overheatState = overheatDecision.state,
                    finalState = lowDecision.state,
                ),
            )
        }
        .launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val tyreWearJob = selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
            tyreWearUseCases.observeTyreWear()
        }
        .onEach { tyreWear ->
            val observedAtMs = currentTimeMs()
            val state = narratorState
            val settings = currentSettings
            val decision = narratorUseCases.determineReadout.determineTyreWear(
                state = state,
                data = tyreWear,
                settings = settings,
            )
            narratorState = decision.state
            eventProcessor.processTyreWear(
                tyreWear = tyreWear,
                events = decision.events,
                readoutOrder = readoutOrder.value,
                queueEnabledStates = queueEnabledStates.value,
                observedAtMs = observedAtMs,
                logContext = LmuWindowsTelemetryLogContext(
                    state = state,
                    settings = settings,
                    finalState = decision.state,
                ),
            )
        }
        .launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val remainingVirtualEnergyLapsJob = combine(
        lmuTelemetryFlow,
        virtualEnergyFlow,
    ) { telemetry, virtualEnergy -> telemetry to virtualEnergy }
        .onEach { (telemetry, virtualEnergy) ->
            val observedAtMs = currentTimeMs()
            val state = narratorState
            val settings = currentSettings
            val decision = narratorUseCases.determineReadout.determineRemainingVirtualEnergyLaps(
                state = state,
                telemetry = telemetry,
                virtualEnergy = virtualEnergy,
                settings = settings,
                observedAtMs = observedAtMs,
            )
            narratorState = decision.state
            eventProcessor.processVirtualEnergy(
                telemetry = telemetry,
                virtualEnergy = virtualEnergy,
                events = decision.events,
                readoutOrder = readoutOrder.value,
                queueEnabledStates = queueEnabledStates.value,
                observedAtMs = observedAtMs,
                logContext = LmuWindowsVirtualEnergyLogContext(
                    state = state,
                    settings = settings,
                    trackingState = decision.state.virtualEnergyTrackingState,
                ),
            )
        }
        .launchIn(viewModelScope)

    private val currentSettings: LmuWindowsNarratorReadoutSettings
        get() = LmuWindowsNarratorReadoutSettings(
            enabledStates = mergedEnabledStates.value,
            myBestLapVoiceType = voiceType.value,
            redFlagVoiceType = redFlagVoiceType.value,
            currentLap = currentLap.value,
            skipFirstLap = skipFirstLap.value,
            vehicleApproachStartReadoutType = startReadoutType.value,
            vehicleApproachSustainedApproachDurationSeconds = sustainedApproachDurationSeconds.value,
            vehicleApproachSustainedReadoutType = sustainedReadoutType.value,
            tyreTemperatureHighThresholdCelsius = tyreHighThreshold.value,
            tyreTemperatureLowWarningPhases = tyreLowWarningPhases.value,
            tyreWearThresholdPercentage = tyreWearThresholdPercentage.value,
            remainingVirtualEnergyLapsThreshold = remainingVirtualEnergyLapsThreshold.value,
            remainingVirtualEnergyLapsEnabled = mergedEnabledStates.value.getOrDefault(
                ReadoutItemKey.LmuWindows.RemainingVirtualEnergyLaps.Root,
                defaultValue = false,
            ),
        )
}
