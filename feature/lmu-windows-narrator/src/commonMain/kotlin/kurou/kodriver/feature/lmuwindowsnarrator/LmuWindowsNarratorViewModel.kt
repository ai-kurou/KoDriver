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
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_DEFAULT_THRESHOLD_PERCENTAGE
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_WEAR_DEFAULT_THRESHOLD_PERCENTAGE
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
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase
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
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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

internal data class RemainingVirtualEnergyUseCases(
    val observeRemainingVirtualEnergy: ObserveLmuWindowsVirtualEnergyUseCase,
    val observeThresholdPercentage: ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase,
)

internal data class PitTimingUseCases(
    val observeVirtualEnergyLapsThreshold: ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase,
    val observeTyreWearLapsThreshold: ObserveLmuWindowsPitTimingTyreWearLapsUseCase,
)

internal data class NarratorUseCases(
    val determineReadout: DetermineLmuWindowsNarratorReadoutUseCase,
    val observeMyBestLapVoiceType: ObserveLmuWindowsMyBestLapVoiceTypeUseCase,
    val observeRedFlagVoiceType: ObserveLmuWindowsRedFlagVoiceTypeUseCase,
)

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
@Suppress("LongParameterList")
internal class LmuWindowsNarratorViewModel(
    vehicleApproachUseCases: VehicleApproachUseCases,
    vehicleDamageUseCases: VehicleDamageUseCases,
    readoutListUseCases: ReadoutListUseCases,
    flagUseCases: FlagUseCases,
    tyreTemperatureUseCases: TyreTemperatureUseCases,
    tyreWearUseCases: TyreWearUseCases,
    remainingVirtualEnergyUseCases: RemainingVirtualEnergyUseCases,
    pitTimingUseCases: PitTimingUseCases,
    private val eventProcessor: LmuWindowsNarratorEventProcessor,
    private val narratorUseCases: NarratorUseCases,
    private val currentTimeMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : ViewModel() {

    private var narratorState = LmuWindowsNarratorState()

    // バーチャルエナジー・タイヤ摩耗のピットタイミング警告は同一ラップ内で1回だけ読み上げる。
    // 別tickで各々が閾値を跨いで先着した場合も、後着の警告で二重読み上げしないようにラップ単位でロックする。
    private var lastAnnouncedPitTimingLap: Int = -1

    private val selectedSimulator =
        readoutListUseCases
        .observeSelectedSimulator()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // listPane（readoutStates）とdetailPane（flagStates・vehicleDamageStates）を統合した、
    // Narratorの読み上げ判定に実際に使う唯一のenabledStates。
    private val mergedEnabledStates =
        combine(
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator == null)
                    emptyFlow<Map<ReadoutItemKey, Boolean>>()
                else
                    readoutListUseCases.observeReadoutEnabledStates(simulator.id)
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

    private val lmuTelemetryFlow =
        selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator !is Simulator.LmuWindows)
                emptyFlow()
            else
                vehicleApproachUseCases.observeLmuWindows()
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

    private val voiceType =
        narratorUseCases
        .observeMyBestLapVoiceType()
        .stateIn(viewModelScope, SharingStarted.Eagerly, MyBestLapVoiceType.FORMAL)

    private val redFlagVoiceType =
        narratorUseCases
        .observeRedFlagVoiceType()
        .stateIn(viewModelScope, SharingStarted.Eagerly, RedFlagVoiceType.SESSION_STOP)

    private val tyreHighThreshold =
        tyreTemperatureUseCases
        .observeHighThreshold()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 95)

    private val tyreLowWarningPhases =
        tyreTemperatureUseCases
        .observeLowWarningPhases()
        .stateIn(viewModelScope, SharingStarted.Eagerly, lmuWindowsTyreTemperatureLowWarningDefaultPhases)

    private val tyreWearThresholdPercentage =
        tyreWearUseCases
        .observeThresholdPercentage()
        .stateIn(viewModelScope, SharingStarted.Eagerly, LMU_WINDOWS_TYRE_WEAR_DEFAULT_THRESHOLD_PERCENTAGE)

    private val remainingVirtualEnergyThresholdPercentage =
        remainingVirtualEnergyUseCases
        .observeThresholdPercentage()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_DEFAULT_THRESHOLD_PERCENTAGE,
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
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val startReadoutType =
        vehicleApproachUseCases
        .observeStartReadoutType()
        .stateIn(viewModelScope, SharingStarted.Eagerly, VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)

    private val sustainedApproachDurationSeconds =
        vehicleApproachUseCases
        .observeSustainedApproachDuration()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT,
        )

    private val sustainedReadoutType =
        vehicleApproachUseCases
        .observeSustainedReadoutType()
        .stateIn(viewModelScope, SharingStarted.Eagerly, VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT)

    @Suppress("UnusedPrivateProperty")
    private val myBestLapJob =
        lmuTelemetryFlow
        .onEach { telemetry ->
            val observedAtMs = currentTimeMs()
            val state = narratorState
            val settings = currentSettings
            val decision =
                narratorUseCases.determineReadout.determineMyBestLap(
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
                narratorUseCases.determineReadout.determineVehicleApproach(
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
                narratorUseCases.determineReadout.determineVehicleDamage(
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
                narratorUseCases.determineReadout.determineRaceFlags(
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
                logContext =
                    LmuWindowsTelemetryLogContext(
                    state = state,
                    settings = settings,
                    finalState = decision.state,
                ),
                    )
        }.launchIn(viewModelScope)

    @Suppress("UnusedPrivateProperty")
    private val tyreTemperatureJob =
        selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
            combine(
                tyreTemperatureUseCases.observeTyreCarcassTemperature(),
                raceFlagsFlow,
            ) { tyreCarcassTemperature, raceFlags -> tyreCarcassTemperature to raceFlags }
        }.onEach { (tyreCarcassTemperature, raceFlags) ->
            val observedAtMs = currentTimeMs()
            val state = narratorState
            val settings = currentSettings
            val overheatDecision =
                narratorUseCases.determineReadout.determineTyreTemperatureOverheat(
                state = state,
                data = tyreCarcassTemperature,
                settings = settings,
            )
            narratorState = overheatDecision.state
            val lowDecision =
                narratorUseCases.determineReadout.determineTyreTemperatureLow(
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
                logContext =
                    LmuWindowsTyreTemperatureLogContext(
                    state = state,
                    settings = settings,
                    overheatState = overheatDecision.state,
                    finalState = lowDecision.state,
                ),
                    )
        }.launchIn(viewModelScope)

    private val tyreWearFlow =
        selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
            tyreWearUseCases.observeTyreWear()
        }.shareIn(viewModelScope, SharingStarted.Eagerly)

    private val virtualEnergyFlow =
        selectedSimulator
        .flatMapLatest { simulator ->
            if (simulator !is Simulator.LmuWindows) return@flatMapLatest emptyFlow()
            remainingVirtualEnergyUseCases.observeRemainingVirtualEnergy()
        }.shareIn(viewModelScope, SharingStarted.Eagerly)

    @Suppress("UnusedPrivateProperty")
    private val tyreWearJob =
        tyreWearFlow
        .onEach { tyreWear ->
            val observedAtMs = currentTimeMs()
            val state = narratorState
            val settings = currentSettings
            val decision =
                narratorUseCases.determineReadout.determineTyreWear(
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
                narratorUseCases.determineReadout.determineRemainingVirtualEnergy(
                state = state,
                data = remainingVirtualEnergy,
                settings = settings,
            )
            narratorState = decision.state
            eventProcessor.processRemainingVirtualEnergy(
                remainingVirtualEnergy = remainingVirtualEnergy,
                events = decision.events,
                readoutOrder = readoutOrder.value,
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
    private val pitTimingJob =
        combine(
        lmuTelemetryFlow,
        virtualEnergyFlow,
        tyreWearFlow,
    ) { telemetry, virtualEnergy, tyreWear -> Triple(telemetry, virtualEnergy, tyreWear) }
        .onEach { (telemetry, virtualEnergy, tyreWear) ->
            val observedAtMs = currentTimeMs()
            val state = narratorState
            val settings = currentSettings
            val virtualEnergyDecision =
                narratorUseCases.determineReadout.determinePitTimingVirtualEnergy(
                state = state,
                telemetry = telemetry,
                virtualEnergy = virtualEnergy,
                settings = settings,
                observedAtMs = observedAtMs,
            )
            narratorState = virtualEnergyDecision.state
            val tyreWearDecision =
                narratorUseCases.determineReadout.determinePitTimingTyreWear(
                state = virtualEnergyDecision.state,
                telemetry = telemetry,
                tyreWear = tyreWear,
                settings = settings,
                observedAtMs = observedAtMs,
            )
            narratorState = tyreWearDecision.state
            val pitTimingEvents =
                if (telemetry.timing.currentLap == lastAnnouncedPitTimingLap) {
                emptyList()
            } else {
                selectLowerPitTimingEvent(virtualEnergyDecision.events, tyreWearDecision.events)
            }
            if (pitTimingEvents.isNotEmpty()) {
                lastAnnouncedPitTimingLap = telemetry.timing.currentLap
            }
            eventProcessor.processPitTiming(
                snapshot =
                    LmuWindowsPitTimingSnapshot(
                    telemetry = telemetry,
                    virtualEnergy = virtualEnergy,
                    tyreWear = tyreWear,
                ),
                    events = pitTimingEvents,
                readoutOrder = readoutOrder.value,
                queueEnabledStates = queueEnabledStates.value,
                observedAtMs = observedAtMs,
                logContext =
                    LmuWindowsPitTimingLogContext(
                    state = state,
                    settings = settings,
                    finalState = tyreWearDecision.state,
                ),
                    )
        }.launchIn(viewModelScope)

    private val currentSettings: LmuWindowsNarratorReadoutSettings
        get() =
            LmuWindowsNarratorReadoutSettings(
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
            remainingVirtualEnergyThresholdPercentage = remainingVirtualEnergyThresholdPercentage.value,
            pitTimingVirtualEnergyLapsThreshold = pitTimingVirtualEnergyLapsThreshold.value,
            pitTimingTyreWearLapsThreshold = pitTimingTyreWearLapsThreshold.value,
        )
}

/**
 * バーチャルエナジー・タイヤ摩耗のピットタイミング警告が同一tickで両方発火した場合、
 * 予想残り周回数が低い方（＝ピット作業がより緊急な方）のみを1回読み上げる。
 */
private fun selectLowerPitTimingEvent(
    virtualEnergyEvents: List<SpeechEvent>,
    tyreWearEvents: List<SpeechEvent>,
): List<SpeechEvent> {
    val virtualEnergyEvent = virtualEnergyEvents.filterIsInstance<SpeechEvent.PitTimingWarning>().firstOrNull()
    val tyreWearEvent = tyreWearEvents.filterIsInstance<SpeechEvent.PitTimingWarning>().firstOrNull()
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
