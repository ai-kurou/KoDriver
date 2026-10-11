package kurou.kodriver.feature.debugstatedetail

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.AceWindowsRemainingFuelLapsData
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveAceWindowsBestLapTimeUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsStatusUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsVehicleApproachUseCase

internal data class AceWindowsDebugStateUseCases(
    val observeFuel: ObserveAceWindowsFuelUseCase,
    val observeFlag: ObserveAceWindowsFlagUseCase,
    val observeStatus: ObserveAceWindowsStatusUseCase,
    val observeTyreCarcassTemperature: ObserveAceWindowsTyreCarcassTemperatureUseCase,
    val observeVehicleApproach: ObserveAceWindowsVehicleApproachUseCase,
    val observeBestLapTime: ObserveAceWindowsBestLapTimeUseCase,
    val observeRemainingFuelLaps: ObserveAceWindowsRemainingFuelLapsUseCase,
)

internal class AceWindowsDebugStateSource(
    scope: CoroutineScope,
    useCases: AceWindowsDebugStateUseCases,
    markCardsReceived: (Simulator, Set<DebugStateCardKey>) -> Unit,
) {
    private val fuel: StateFlow<AceWindowsFuelData?> =
        useCases
            .observeFuel()
            .onEach {
                markCardsReceived(Simulator.AceWindows, setOf(DebugStateCardKey.FUEL_CONSUMPTION))
            }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), null)

    private val flag: StateFlow<AceWindowsFlagData?> =
        useCases
            .observeFlag()
            .onEach {
                markCardsReceived(Simulator.AceWindows, setOf(DebugStateCardKey.FLAG_INFO))
            }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), null)

    private val status: StateFlow<AceWindowsStatusData?> =
        useCases
            .observeStatus()
            .onEach {
                markCardsReceived(Simulator.AceWindows, setOf(DebugStateCardKey.VEHICLE_LOCATION))
            }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), null)

    private val tyreCarcassTemperature: StateFlow<AceWindowsTyreCarcassTemperatureData?> =
        useCases
            .observeTyreCarcassTemperature()
            .onEach {
                markCardsReceived(Simulator.AceWindows, setOf(DebugStateCardKey.TYRE_CARCASS_TEMPERATURE))
            }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), null)

    private val vehicleApproach: StateFlow<AceWindowsVehicleApproachData?> =
        useCases
            .observeVehicleApproach()
            .onEach {
                markCardsReceived(Simulator.AceWindows, setOf(DebugStateCardKey.SIDE_BY_SIDE_VEHICLES))
            }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), null)

    private val bestLapTime: StateFlow<AceWindowsBestLapTimeData?> =
        useCases
            .observeBestLapTime()
            .onEach {
                markCardsReceived(Simulator.AceWindows, setOf(DebugStateCardKey.BEST_LAP))
            }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), null)

    private val remainingFuelLaps: StateFlow<AceWindowsRemainingFuelLapsData?> =
        useCases
            .observeRemainingFuelLaps()
            .onEach {
                markCardsReceived(Simulator.AceWindows, setOf(DebugStateCardKey.FUEL_CONSUMPTION))
            }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), null)

    private val base: StateFlow<AceWindowsDebugState> =
        combine(
            fuel,
            flag,
            status,
            bestLapTime,
            remainingFuelLaps,
        ) { fuel, flag, status, bestLapTime, remainingFuelLaps ->
            AceWindowsDebugState(
                fuel = fuel,
                flag = flag,
                status = status,
                bestLapTime = bestLapTime,
                remainingFuelLaps = remainingFuelLaps,
            )
        }.stateIn(scope, SharingStarted.Eagerly, AceWindowsDebugState())

    val state: StateFlow<AceWindowsDebugState> =
        combine(base, vehicleApproach, tyreCarcassTemperature) { base, vehicleApproach, tyreCarcassTemperature ->
            base.copy(
                vehicleApproach = vehicleApproach,
                tyreCarcassTemperature = tyreCarcassTemperature,
            )
        }.stateIn(scope, SharingStarted.Eagerly, AceWindowsDebugState())
}
