package kurou.kodriver.feature.debugstatedetail

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
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTyreDetachedData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeWearRemainingUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitStatusUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreDetachedUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVirtualEnergyUseCase

internal data class LmuWindowsDebugStateUseCases(
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
    markCardsReceived: (Simulator, Set<DebugStateCardKey>) -> Unit,
    currentTimeMs: () -> Long,
    sideBySideTickIntervalMs: Long,
) {
    private val sideBySideDurationTracker = LmuWindowsSideBySideDurationTracker()
    private val _lmuWindowsVehicleApproach =
        useCases.observeVehicleApproach().shareIn(scope, SharingStarted.Eagerly, replay = 1)
    val sideBySideDurations: StateFlow<LmuWindowsSideBySideDurations?> =
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

    private val _raceStateBase: StateFlow<DebugStateDetailUiState> =
        combine(
            useCases
                .observeRaceFlags()
                .onEach {
                    markCardsReceived(
                        Simulator.LmuWindows,
                        setOf(
                            DebugStateCardKey.FLAG_INFO,
                            DebugStateCardKey.GAME_PHASE,
                            DebugStateCardKey.YELLOW_FLAG_STATE,
                        ),
                    )
                },
            useCases
                .observeVirtualEnergy()
                .onEach {
                    markCardsReceived(
                        Simulator.LmuWindows,
                        setOf(
                            DebugStateCardKey.SESSION,
                            DebugStateCardKey.FUEL_CONSUMPTION,
                            DebugStateCardKey.PIT_TIMING_REMAINING_LAPS,
                        ),
                    )
                },
            _lmuWindowsVehicleApproach
                .onEach {
                    markCardsReceived(Simulator.LmuWindows, setOf(DebugStateCardKey.SIDE_BY_SIDE_VEHICLES))
                },
            useCases
                .observeTyreCarcassTemperature()
                .onEach {
                    markCardsReceived(Simulator.LmuWindows, setOf(DebugStateCardKey.TYRE_CARCASS_TEMPERATURE))
                },
            useCases
                .observeVehicleClass()
                .onEach {
                    markCardsReceived(Simulator.LmuWindows, setOf(DebugStateCardKey.VEHICLE_CLASS))
                },
        ) { raceFlags, virtualEnergy, vehicleApproach, tyreCarcassTemperature, lmuWindowsVehicleClass ->
            DebugStateDetailUiState(
                raceFlags = raceFlags,
                virtualEnergy = virtualEnergy,
                vehicleApproach = vehicleApproach,
                tyreCarcassTemperature = tyreCarcassTemperature,
                lmuWindowsVehicleClass = lmuWindowsVehicleClass,
            )
        }.stateIn(
            scope,
            SharingStarted.Eagerly,
            DebugStateDetailUiState(),
        )

    private val _lmuWindowsBrakeTemperature: StateFlow<LmuWindowsBrakeTemperatureData?> =
        useCases
            .observeBrakeTemperature()
            .onEach {
                markCardsReceived(Simulator.LmuWindows, setOf(DebugStateCardKey.BRAKE_TEMPERATURE))
            }.stateIn(scope, SharingStarted.Eagerly, null)

    private val _lmuWindowsBrakeWear: StateFlow<LmuWindowsBrakeWearRemainingData?> =
        useCases
            .observeBrakeWear()
            .onEach {
                markCardsReceived(Simulator.LmuWindows, setOf(DebugStateCardKey.BRAKE_WEAR))
            }.stateIn(scope, SharingStarted.Eagerly, null)

    private val _lmuWindowsPitStatus: StateFlow<LmuWindowsPitStatusData?> =
        useCases
            .observePitStatus()
            .onEach {
                markCardsReceived(Simulator.LmuWindows, setOf(DebugStateCardKey.VEHICLE_LOCATION))
            }.stateIn(scope, SharingStarted.Eagerly, null)

    private val _lmuWindowsVehicleDamage: StateFlow<LmuWindowsVehicleDamageData?> =
        useCases
            .observeVehicleDamage()
            .onEach {
                markCardsReceived(Simulator.LmuWindows, setOf(DebugStateCardKey.VEHICLE_DAMAGE))
            }.stateIn(scope, SharingStarted.Eagerly, null)

    private val _lmuWindowsTyreDetached: StateFlow<LmuWindowsTyreDetachedData?> =
        useCases
            .observeTyreDetached()
            .onEach {
                markCardsReceived(Simulator.LmuWindows, setOf(DebugStateCardKey.VEHICLE_DAMAGE))
            }.stateIn(scope, SharingStarted.Eagerly, null)

    private val _raceStateWithoutBrakeWear: StateFlow<DebugStateDetailUiState> =
        combine(
            _raceStateBase,
            _lmuWindowsPitStatus,
            _lmuWindowsVehicleDamage,
            _lmuWindowsTyreDetached,
            _lmuWindowsBrakeTemperature,
        ) { base, lmuWindowsPitStatus, vehicleDamage, tyreDetached, brakeTemperature ->
            base.copy(
                lmuWindowsPitStatus = lmuWindowsPitStatus,
                vehicleDamage = vehicleDamage,
                tyreDetached = tyreDetached,
                brakeTemperature = brakeTemperature,
            )
        }.stateIn(
            scope,
            SharingStarted.Eagerly,
            DebugStateDetailUiState(),
        )

    val raceState: StateFlow<DebugStateDetailUiState> =
        combine(_raceStateWithoutBrakeWear, _lmuWindowsBrakeWear) { base, brakeWear ->
            base.copy(brakeWear = brakeWear)
        }.stateIn(
            scope,
            SharingStarted.Eagerly,
            DebugStateDetailUiState(),
        )

    val telemetry: StateFlow<LmuWindowsTelemetryData?> =
        useCases
            .observeTelemetry()
            .onEach {
                markCardsReceived(
                    Simulator.LmuWindows,
                    setOf(
                        DebugStateCardKey.CURRENT_LAP,
                        DebugStateCardKey.BEST_LAP,
                        DebugStateCardKey.TYRE_TEMPERATURE,
                        DebugStateCardKey.TYRE_WEAR,
                        DebugStateCardKey.FUEL_CONSUMPTION,
                        DebugStateCardKey.PIT_TIMING_REMAINING_LAPS,
                    ),
                )
            }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), null)
}
