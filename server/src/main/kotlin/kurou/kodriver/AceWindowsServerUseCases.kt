package kurou.kodriver

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.domain.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.AceWindowsRemainingFuelLapsData
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.repository.AceWindowsBestLapTimeRepository
import kurou.kodriver.domain.repository.AceWindowsFlagRepository
import kurou.kodriver.domain.repository.AceWindowsFuelRepository
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelLapsRepository
import kurou.kodriver.domain.repository.AceWindowsStatusRepository
import kurou.kodriver.domain.repository.AceWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachRepository
import kurou.kodriver.domain.usecase.ObserveAceWindowsBestLapTimeUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsStatusUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsVehicleApproachUseCase
import org.koin.core.Koin

/** ACE の WebSocket エンドポイントの購読用 UseCase。 */
data class AceWindowsServerUseCases(
    val observeAceWindowsFuel: ObserveAceWindowsFuelUseCase,
    val observeAceWindowsFlag: ObserveAceWindowsFlagUseCase,
    val observeAceWindowsStatus: ObserveAceWindowsStatusUseCase,
    val observeAceWindowsTyreCarcassTemperature: ObserveAceWindowsTyreCarcassTemperatureUseCase,
    val observeAceWindowsVehicleApproach: ObserveAceWindowsVehicleApproachUseCase,
    val observeAceWindowsBestLapTime: ObserveAceWindowsBestLapTimeUseCase,
    val observeAceWindowsRemainingFuelLaps: ObserveAceWindowsRemainingFuelLapsUseCase,
)

internal fun aceWindowsServerUseCasesFrom(koin: Koin): AceWindowsServerUseCases =
    AceWindowsServerUseCases(
        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(koin.get<AceWindowsFuelRepository>()),
        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(koin.get<AceWindowsFlagRepository>()),
        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(koin.get<AceWindowsStatusRepository>()),
        observeAceWindowsTyreCarcassTemperature =
            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                koin.get<AceWindowsTyreCarcassTemperatureRepository>(),
            ),
        observeAceWindowsVehicleApproach =
            ObserveAceWindowsVehicleApproachUseCase(
                koin.get<AceWindowsVehicleApproachRepository>(),
            ),
        observeAceWindowsBestLapTime =
            ObserveAceWindowsBestLapTimeUseCase(
                koin.get<AceWindowsBestLapTimeRepository>(),
            ),
        observeAceWindowsRemainingFuelLaps =
            ObserveAceWindowsRemainingFuelLapsUseCase(
                koin.get<AceWindowsRemainingFuelLapsRepository>(),
            ),
    )

internal fun emptyAceWindowsServerUseCases(): AceWindowsServerUseCases =
    AceWindowsServerUseCases(
        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(EmptyAceWindowsFuelRepository),
        observeAceWindowsFlag = ObserveAceWindowsFlagUseCase(EmptyAceWindowsFlagRepository),
        observeAceWindowsStatus = ObserveAceWindowsStatusUseCase(EmptyAceWindowsStatusRepository),
        observeAceWindowsTyreCarcassTemperature =
            ObserveAceWindowsTyreCarcassTemperatureUseCase(
                EmptyAceWindowsTyreCarcassTemperatureRepository,
            ),
        observeAceWindowsVehicleApproach =
            ObserveAceWindowsVehicleApproachUseCase(
                EmptyAceWindowsVehicleApproachRepository,
            ),
        observeAceWindowsBestLapTime =
            ObserveAceWindowsBestLapTimeUseCase(
                EmptyAceWindowsBestLapTimeRepository,
            ),
        observeAceWindowsRemainingFuelLaps =
            ObserveAceWindowsRemainingFuelLapsUseCase(
                EmptyAceWindowsRemainingFuelLapsRepository,
            ),
    )

private object EmptyAceWindowsFuelRepository : AceWindowsFuelRepository {
    override fun fuelStream(): Flow<AceWindowsFuelData> = emptyFlow()

    override suspend fun isConnected(): Boolean = false
}

private object EmptyAceWindowsFlagRepository : AceWindowsFlagRepository {
    override fun flagStream(): Flow<AceWindowsFlagData> = emptyFlow()
}

private object EmptyAceWindowsStatusRepository : AceWindowsStatusRepository {
    override fun statusStream(): Flow<AceWindowsStatusData> = emptyFlow()
}

private object EmptyAceWindowsTyreCarcassTemperatureRepository : AceWindowsTyreCarcassTemperatureRepository {
    override fun tyreCarcassTemperatureStream(): Flow<AceWindowsTyreCarcassTemperatureData> = emptyFlow()
}

private object EmptyAceWindowsVehicleApproachRepository : AceWindowsVehicleApproachRepository {
    override fun vehicleApproachStream(): Flow<AceWindowsVehicleApproachData> = emptyFlow()
}

private object EmptyAceWindowsBestLapTimeRepository : AceWindowsBestLapTimeRepository {
    override fun bestLapTimeStream(): Flow<AceWindowsBestLapTimeData> = emptyFlow()
}

private object EmptyAceWindowsRemainingFuelLapsRepository : AceWindowsRemainingFuelLapsRepository {
    override fun remainingFuelLapsStream(): Flow<AceWindowsRemainingFuelLapsData> = emptyFlow()
}
