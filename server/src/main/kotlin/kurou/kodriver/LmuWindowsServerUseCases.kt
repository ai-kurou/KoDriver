package kurou.kodriver

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeWearData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreDetachedData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsBrakeTemperatureRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsBrakeWearRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitStatusRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreDetachedRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreWearRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVirtualEnergyRepository
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeTemperatureUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeWearUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitStatusUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreDetachedUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreWearUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVirtualEnergyUseCase
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
