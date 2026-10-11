package kurou.kodriver

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.domain.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.model.LmuWindowsBrakeWearData
import kurou.kodriver.domain.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsTyreDetachedData
import kurou.kodriver.domain.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.repository.LmuWindowsBrakeTemperatureRepository
import kurou.kodriver.domain.repository.LmuWindowsBrakeWearRepository
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.repository.LmuWindowsPitStatusRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreDetachedRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreWearRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.repository.LmuWindowsVirtualEnergyRepository
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeWearUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitStatusUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreDetachedUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleClassUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import org.koin.core.Koin

/** LMU の WebSocket エンドポイントの購読用 UseCase。 */
data class LmuWindowsServerUseCases(
    val observeLmuWindowsRaceFlags: ObserveLmuWindowsRaceFlagsUseCase,
    val observeLmuWindowsVehicleApproach: ObserveLmuWindowsVehicleApproachUseCase,
    val observeLmuWindowsVehicleDamage: ObserveLmuWindowsVehicleDamageUseCase,
    val observeLmuWindowsTyreCarcassTemperature: ObserveLmuWindowsTyreCarcassTemperatureUseCase,
    val observeLmuWindowsBrakeTemperature: ObserveLmuWindowsBrakeTemperatureUseCase,
    val observeLmuWindowsVehicleClass: ObserveLmuWindowsVehicleClassUseCase,
    val observeLmuWindowsTyreWear: ObserveLmuWindowsTyreWearUseCase,
    val observeLmuWindows: ObserveLmuWindowsUseCase,
    val observeLmuWindowsVirtualEnergy: ObserveLmuWindowsVirtualEnergyUseCase,
    val observeLmuWindowsPitStatus: ObserveLmuWindowsPitStatusUseCase,
    val observeLmuWindowsBrakeWear: ObserveLmuWindowsBrakeWearUseCase,
    val observeLmuWindowsTyreDetached: ObserveLmuWindowsTyreDetachedUseCase,
)

internal fun lmuWindowsServerUseCasesFrom(koin: Koin): LmuWindowsServerUseCases =
    LmuWindowsServerUseCases(
        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(koin.get<LmuWindowsFlagRepository>()),
        observeLmuWindowsVehicleApproach =
            ObserveLmuWindowsVehicleApproachUseCase(
                koin.get<LmuWindowsVehicleApproachRepository>(),
            ),
        observeLmuWindowsVehicleDamage =
            ObserveLmuWindowsVehicleDamageUseCase(
                koin.get<LmuWindowsVehicleDamageRepository>(),
            ),
        observeLmuWindowsTyreCarcassTemperature =
            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                koin.get<LmuWindowsTyreCarcassTemperatureRepository>(),
            ),
        observeLmuWindowsBrakeTemperature =
            ObserveLmuWindowsBrakeTemperatureUseCase(
                koin.get<LmuWindowsBrakeTemperatureRepository>(),
            ),
        observeLmuWindowsVehicleClass =
            ObserveLmuWindowsVehicleClassUseCase(
                koin.get<LmuWindowsVehicleClassRepository>(),
            ),
        observeLmuWindowsTyreWear =
            ObserveLmuWindowsTyreWearUseCase(
                koin.get<LmuWindowsTyreWearRepository>(),
            ),
        observeLmuWindows = ObserveLmuWindowsUseCase(koin.get<LmuWindowsRepository>()),
        observeLmuWindowsVirtualEnergy =
            ObserveLmuWindowsVirtualEnergyUseCase(
                koin.get<LmuWindowsVirtualEnergyRepository>(),
            ),
        observeLmuWindowsPitStatus =
            ObserveLmuWindowsPitStatusUseCase(
                koin.get<LmuWindowsPitStatusRepository>(),
            ),
        observeLmuWindowsBrakeWear =
            ObserveLmuWindowsBrakeWearUseCase(koin.get<LmuWindowsBrakeWearRepository>()),
        observeLmuWindowsTyreDetached =
            ObserveLmuWindowsTyreDetachedUseCase(
                koin.get<LmuWindowsTyreDetachedRepository>(),
            ),
    )

internal fun emptyLmuWindowsServerUseCases(): LmuWindowsServerUseCases =
    LmuWindowsServerUseCases(
        observeLmuWindowsRaceFlags = ObserveLmuWindowsRaceFlagsUseCase(EmptyLmuWindowsFlagRepository),
        observeLmuWindowsVehicleApproach =
            ObserveLmuWindowsVehicleApproachUseCase(
                EmptyLmuWindowsVehicleApproachRepository,
            ),
        observeLmuWindowsVehicleDamage =
            ObserveLmuWindowsVehicleDamageUseCase(
                EmptyLmuWindowsVehicleDamageRepository,
            ),
        observeLmuWindowsTyreCarcassTemperature =
            ObserveLmuWindowsTyreCarcassTemperatureUseCase(
                EmptyLmuWindowsTyreCarcassTemperatureRepository,
            ),
        observeLmuWindowsBrakeTemperature =
            ObserveLmuWindowsBrakeTemperatureUseCase(
                EmptyLmuWindowsBrakeTemperatureRepository,
            ),
        observeLmuWindowsVehicleClass =
            ObserveLmuWindowsVehicleClassUseCase(
                EmptyLmuWindowsVehicleClassRepository,
            ),
        observeLmuWindowsTyreWear = ObserveLmuWindowsTyreWearUseCase(EmptyLmuWindowsTyreWearRepository),
        observeLmuWindows = ObserveLmuWindowsUseCase(EmptyLmuWindowsRepository),
        observeLmuWindowsVirtualEnergy =
            ObserveLmuWindowsVirtualEnergyUseCase(
                EmptyLmuWindowsVirtualEnergyRepository,
            ),
        observeLmuWindowsPitStatus =
            ObserveLmuWindowsPitStatusUseCase(
                EmptyLmuWindowsPitStatusRepository,
            ),
        observeLmuWindowsBrakeWear =
            ObserveLmuWindowsBrakeWearUseCase(EmptyLmuWindowsBrakeWearRepository),
        observeLmuWindowsTyreDetached =
            ObserveLmuWindowsTyreDetachedUseCase(
                EmptyLmuWindowsTyreDetachedRepository,
            ),
    )

private object EmptyLmuWindowsFlagRepository : LmuWindowsFlagRepository {
    override fun flagStream(): Flow<LmuWindowsRaceFlagsData> = emptyFlow()
}

private object EmptyLmuWindowsVehicleApproachRepository : LmuWindowsVehicleApproachRepository {
    override fun vehicleApproachStream(): Flow<LmuWindowsVehicleApproachData> = emptyFlow()
}

private object EmptyLmuWindowsVehicleDamageRepository : LmuWindowsVehicleDamageRepository {
    override fun vehicleDamageStream(): Flow<LmuWindowsVehicleDamageData> = emptyFlow()
}

private object EmptyLmuWindowsTyreCarcassTemperatureRepository : LmuWindowsTyreCarcassTemperatureRepository {
    override fun tyreCarcassTemperatureStream(): Flow<LmuWindowsTyreCarcassTemperatureData> = emptyFlow()
}

private object EmptyLmuWindowsBrakeTemperatureRepository : LmuWindowsBrakeTemperatureRepository {
    override fun brakeTemperatureStream(): Flow<LmuWindowsBrakeTemperatureData> = emptyFlow()
}

private object EmptyLmuWindowsVehicleClassRepository : LmuWindowsVehicleClassRepository {
    override fun vehicleClassStream(): Flow<LmuWindowsVehicleClassData> = emptyFlow()
}

private object EmptyLmuWindowsTyreWearRepository : LmuWindowsTyreWearRepository {
    override fun tyreWearStream(): Flow<LmuWindowsTyreWearData> = emptyFlow()
}

private object EmptyLmuWindowsRepository : LmuWindowsRepository {
    override fun telemetryStream(): Flow<LmuWindowsTelemetryData> = emptyFlow()

    override suspend fun isConnected(): Boolean = false

    override suspend fun disconnect() = Unit
}

private object EmptyLmuWindowsVirtualEnergyRepository : LmuWindowsVirtualEnergyRepository {
    override fun virtualEnergyStream(): Flow<LmuWindowsVirtualEnergyData> = emptyFlow()
}

private object EmptyLmuWindowsPitStatusRepository : LmuWindowsPitStatusRepository {
    override fun pitStatusStream(): Flow<LmuWindowsPitStatusData> = emptyFlow()
}

private object EmptyLmuWindowsTyreDetachedRepository : LmuWindowsTyreDetachedRepository {
    override fun tyreDetachedStream(): Flow<LmuWindowsTyreDetachedData> = emptyFlow()
}

private object EmptyLmuWindowsBrakeWearRepository : LmuWindowsBrakeWearRepository {
    override fun brakeWearStream(): Flow<LmuWindowsBrakeWearData?> = emptyFlow()
}
