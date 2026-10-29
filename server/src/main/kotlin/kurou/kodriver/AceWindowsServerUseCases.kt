package kurou.kodriver

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.domain.acewindows.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagData
import kurou.kodriver.domain.acewindows.model.AceWindowsFuelData
import kurou.kodriver.domain.acewindows.model.AceWindowsGapData
import kurou.kodriver.domain.acewindows.model.AceWindowsOverallPositionData
import kurou.kodriver.domain.acewindows.model.AceWindowsRemainingFuelLapsData
import kurou.kodriver.domain.acewindows.model.AceWindowsStatusData
import kurou.kodriver.domain.acewindows.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.acewindows.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.acewindows.repository.AceWindowsBestLapTimeRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsFuelRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsGapRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsOverallPositionRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsStatusRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachRepository
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsBestLapTimeUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsFlagUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsFuelUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsGapUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsOverallPositionUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelLapsUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsStatusUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsVehicleApproachUseCase
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
    val observeAceWindowsGap: ObserveAceWindowsGapUseCase,
    val observeAceWindowsOverallPosition: ObserveAceWindowsOverallPositionUseCase,
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
        observeAceWindowsGap = ObserveAceWindowsGapUseCase(koin.get<AceWindowsGapRepository>()),
        observeAceWindowsOverallPosition =
            ObserveAceWindowsOverallPositionUseCase(
                koin.get<AceWindowsOverallPositionRepository>(),
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
        observeAceWindowsGap = ObserveAceWindowsGapUseCase(EmptyAceWindowsGapRepository),
        observeAceWindowsOverallPosition =
            ObserveAceWindowsOverallPositionUseCase(
                EmptyAceWindowsOverallPositionRepository,
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

private object EmptyAceWindowsGapRepository : AceWindowsGapRepository {
    override fun gapStream(): Flow<AceWindowsGapData> = emptyFlow()
}

private object EmptyAceWindowsOverallPositionRepository : AceWindowsOverallPositionRepository {
    override fun overallPositionStream(): Flow<AceWindowsOverallPositionData> = emptyFlow()
}
