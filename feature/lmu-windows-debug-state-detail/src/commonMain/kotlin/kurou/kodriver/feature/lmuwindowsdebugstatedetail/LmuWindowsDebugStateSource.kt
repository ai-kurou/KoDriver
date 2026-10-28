package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsCarGapData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsClassCarGapData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreDetachedData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeTemperatureUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeWearRemainingUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsCarGapUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsClassCarGapUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitStatusUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreDetachedUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVirtualEnergyUseCase

internal data class LmuWindowsDebugStateUseCases(
    val observeCarGap: ObserveLmuWindowsCarGapUseCase,
    val observeClassCarGap: ObserveLmuWindowsClassCarGapUseCase,
    val observeRaceFlags: ObserveLmuWindowsRaceFlagsUseCase,
    val observeVirtualEnergy: ObserveLmuWindowsVirtualEnergyUseCase,
    val observeTelemetry: ObserveLmuWindowsUseCase,
    val observeVehicleApproach: ObserveLmuWindowsVehicleApproachUseCase,
    val observeTyreCarcassTemperature: ObserveLmuWindowsTyreCarcassTemperatureUseCase,
    val observeBrakeWear: ObserveLmuWindowsBrakeWearRemainingUseCase,
    val observeBrakeTemperature: ObserveLmuWindowsBrakeTemperatureUseCase,
    val observeVehicleClass: ObserveLmuWindowsVehicleClassUseCase,
    val observePitStatus: ObserveLmuWindowsPitStatusUseCase,
    val observeVehicleDamage: ObserveLmuWindowsVehicleDamageUseCase,
    val observeTyreDetached: ObserveLmuWindowsTyreDetachedUseCase,
)

@OptIn(ExperimentalCoroutinesApi::class)
internal class LmuWindowsDebugStateSource(
    scope: CoroutineScope,
    useCases: LmuWindowsDebugStateUseCases,
    markCardsReceived: (Set<LmuWindowsDebugStateCardKey>) -> Unit,
    currentTimeMs: () -> Long,
    sideBySideTickIntervalMs: Long,
) {
    private val sideBySideDurationTracker = LmuWindowsSideBySideDurationTracker()
    private val virtualEnergyBaselineTracker = LmuWindowsVirtualEnergyBaselineTracker()
    private val tyreWearBaselineTracker = LmuWindowsTyreWearBaselineTracker()
    private val fuelBaselineTracker = LmuWindowsFuelBaselineTracker()
    private val _lmuWindowsVehicleApproach =
        useCases.observeVehicleApproach().shareIn(scope, SharingStarted.Eagerly, replay = 1)
    private val sideBySideDurations: StateFlow<LmuWindowsSideBySideDurations?> =
        _lmuWindowsVehicleApproach
            .flatMapLatest { data ->
                if (data.sideBySideLeftVehicleIds.isEmpty() && data.sideBySideRightVehicleIds.isEmpty()) {
                    flowOf(sideBySideDurationTracker.update(data, currentTimeMs()))
                } else {
                    // サーバー側の distinctUntilChanged で同一データが再送されないため、並走中は周期的に経過時間を再計算する
                    flow {
                        while (true) {
                            emit(sideBySideDurationTracker.update(data, currentTimeMs()))
                            delay(sideBySideTickIntervalMs)
                        }
                    }
                }
            }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), null)

    private val _raceStateBase: StateFlow<LmuWindowsDebugState> =
        combine(
            useCases
                .observeRaceFlags()
                .onEach {
                    markCardsReceived(
                        setOf(
                            LmuWindowsDebugStateCardKey.FLAG_INFO,
                            LmuWindowsDebugStateCardKey.GAME_PHASE,
                            LmuWindowsDebugStateCardKey.YELLOW_FLAG_STATE,
                        ),
                    )
                },
            useCases
                .observeVirtualEnergy()
                .onEach {
                    markCardsReceived(
                        setOf(
                            LmuWindowsDebugStateCardKey.SESSION,
                            LmuWindowsDebugStateCardKey.VIRTUAL_ENERGY,
                            LmuWindowsDebugStateCardKey.PIT_TIMING_REMAINING_LAPS,
                        ),
                    )
                },
            _lmuWindowsVehicleApproach
                .onEach {
                    markCardsReceived(setOf(LmuWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES))
                },
            useCases
                .observeTyreCarcassTemperature()
                .onEach {
                    markCardsReceived(setOf(LmuWindowsDebugStateCardKey.TYRE_CARCASS_TEMPERATURE))
                },
            useCases
                .observeVehicleClass()
                .onEach {
                    markCardsReceived(setOf(LmuWindowsDebugStateCardKey.VEHICLE_CLASS))
                },
        ) { raceFlags, virtualEnergy, vehicleApproach, tyreCarcassTemperature, vehicleClass ->
            LmuWindowsDebugState(
                raceFlags = raceFlags,
                virtualEnergy = virtualEnergy,
                vehicleApproach = vehicleApproach,
                tyreCarcassTemperature = tyreCarcassTemperature,
                vehicleClass = vehicleClass,
            )
        }.stateIn(
            scope,
            SharingStarted.Eagerly,
            LmuWindowsDebugState(),
        )

    private val _lmuWindowsBrakeTemperature: StateFlow<LmuWindowsBrakeTemperatureData?> =
        useCases
            .observeBrakeTemperature()
            .onEach {
                markCardsReceived(setOf(LmuWindowsDebugStateCardKey.BRAKE_TEMPERATURE))
            }.stateIn(scope, SharingStarted.Eagerly, null)

    private val _lmuWindowsBrakeWear: StateFlow<LmuWindowsBrakeWearRemainingData?> =
        useCases
            .observeBrakeWear()
            .onEach {
                markCardsReceived(setOf(LmuWindowsDebugStateCardKey.BRAKE_WEAR))
            }.stateIn(scope, SharingStarted.Eagerly, null)

    private val _lmuWindowsPitStatus: StateFlow<LmuWindowsPitStatusData?> =
        useCases
            .observePitStatus()
            .onEach {
                markCardsReceived(setOf(LmuWindowsDebugStateCardKey.VEHICLE_LOCATION))
            }.stateIn(scope, SharingStarted.Eagerly, null)

    private val _lmuWindowsVehicleDamage: StateFlow<LmuWindowsVehicleDamageData?> =
        useCases
            .observeVehicleDamage()
            .onEach {
                markCardsReceived(setOf(LmuWindowsDebugStateCardKey.VEHICLE_DAMAGE))
            }.stateIn(scope, SharingStarted.Eagerly, null)

    private val _lmuWindowsTyreDetached: StateFlow<LmuWindowsTyreDetachedData?> =
        useCases
            .observeTyreDetached()
            .onEach {
                markCardsReceived(setOf(LmuWindowsDebugStateCardKey.VEHICLE_DAMAGE))
            }.stateIn(scope, SharingStarted.Eagerly, null)

    private val _raceStateWithoutBrakeWear: StateFlow<LmuWindowsDebugState> =
        combine(
            _raceStateBase,
            _lmuWindowsPitStatus,
            _lmuWindowsVehicleDamage,
            _lmuWindowsTyreDetached,
            _lmuWindowsBrakeTemperature,
        ) { base, pitStatus, vehicleDamage, tyreDetached, brakeTemperature ->
            base.copy(
                pitStatus = pitStatus,
                vehicleDamage = vehicleDamage,
                tyreDetached = tyreDetached,
                brakeTemperature = brakeTemperature,
            )
        }.stateIn(
            scope,
            SharingStarted.Eagerly,
            LmuWindowsDebugState(),
        )

    private val _carGap: StateFlow<LmuWindowsCarGapData?> =
        useCases
            .observeCarGap()
            .onEach {
                markCardsReceived(setOf(LmuWindowsDebugStateCardKey.CAR_GAP))
            }.stateIn(scope, SharingStarted.Eagerly, null)

    private val _classCarGap: StateFlow<LmuWindowsClassCarGapData?> =
        useCases
            .observeClassCarGap()
            .onEach {
                markCardsReceived(setOf(LmuWindowsDebugStateCardKey.CLASS_CAR_GAP))
            }.stateIn(scope, SharingStarted.Eagerly, null)

    private val _raceState: StateFlow<LmuWindowsDebugState> =
        combine(
            _raceStateWithoutBrakeWear,
            _lmuWindowsBrakeWear,
            _carGap,
            _classCarGap,
        ) { base, brakeWear, carGap, classCarGap ->
            base.copy(brakeWear = brakeWear, carGap = carGap, classCarGap = classCarGap)
        }.stateIn(
            scope,
            SharingStarted.Eagerly,
            LmuWindowsDebugState(),
        )

    private val telemetry: StateFlow<LmuWindowsTelemetryData?> =
        useCases
            .observeTelemetry()
            .onEach {
                markCardsReceived(
                    setOf(
                        LmuWindowsDebugStateCardKey.CURRENT_LAP,
                        LmuWindowsDebugStateCardKey.BEST_LAP,
                        LmuWindowsDebugStateCardKey.TYRE_TEMPERATURE,
                        LmuWindowsDebugStateCardKey.TYRE_WEAR,
                        LmuWindowsDebugStateCardKey.FUEL_CONSUMPTION,
                        LmuWindowsDebugStateCardKey.VIRTUAL_ENERGY,
                        LmuWindowsDebugStateCardKey.PIT_TIMING_REMAINING_LAPS,
                    ),
                )
            }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), null)

    private val base: StateFlow<LmuWindowsDebugState> =
        combine(_raceState, telemetry) { race, telemetry ->
            race.copy(
                telemetry = telemetry,
                fuelBaseline = fuelBaselineTracker.update(telemetry),
                virtualEnergyBaseline = virtualEnergyBaselineTracker.update(race.virtualEnergy, telemetry),
                tyreWearBaseline = tyreWearBaselineTracker.update(telemetry),
            )
        }.stateIn(scope, SharingStarted.Eagerly, LmuWindowsDebugState())

    val state: StateFlow<LmuWindowsDebugState> =
        combine(base, sideBySideDurations) { base, durations ->
            base.copy(sideBySideDurations = durations)
        }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), LmuWindowsDebugState())
}
