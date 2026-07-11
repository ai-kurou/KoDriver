package kurou.kodriver.core.lmuwindowsdata

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.core.lmuwindowsdata.repository.LmuWindowsFlagRepositoryImpl
import kurou.kodriver.core.lmuwindowsdata.repository.LmuWindowsRepositoryImpl
import kurou.kodriver.core.lmuwindowsdata.repository.LmuWindowsTyreCarcassTemperatureRepositoryImpl
import kurou.kodriver.core.lmuwindowsdata.repository.LmuWindowsVehicleApproachRepositoryImpl
import kurou.kodriver.core.lmuwindowsdata.repository.LmuWindowsVehicleDamageRepositoryImpl
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamageRepository
import org.koin.dsl.module

private val isWindows = System.getProperty("os.name").lowercase().startsWith("windows")

val lmuWindowsDataModule = module {
    single { CoroutineScope(SupervisorJob()) }
    single { LmuWindowsSharedMemorySource(scope = get()) }
    single<LmuWindowsRepository> {
        if (isWindows) LmuWindowsRepositoryImpl(source = get()) else NoOpLmuWindowsRepository()
    }
    single<LmuWindowsVehicleApproachRepository> {
        if (isWindows) {
            LmuWindowsVehicleApproachRepositoryImpl(thresholdsRepository = get(), source = get())
        } else {
            NoOpVehicleApproachRepository()
        }
    }
    single<LmuWindowsFlagRepository> {
        if (isWindows) LmuWindowsFlagRepositoryImpl(source = get()) else NoOpFlagRepository()
    }
    single<LmuWindowsVehicleDamageRepository> {
        if (isWindows) LmuWindowsVehicleDamageRepositoryImpl(source = get()) else NoOpVehicleDamageRepository()
    }
    single<LmuWindowsTyreCarcassTemperatureRepository> {
        if (isWindows) {
            LmuWindowsTyreCarcassTemperatureRepositoryImpl(source = get())
        } else {
            NoOpTyreCarcassTemperatureRepository()
        }
    }
}

private class NoOpLmuWindowsRepository : LmuWindowsRepository {
    override fun telemetryStream(): Flow<LmuWindowsTelemetryData> = emptyFlow()
    override suspend fun isConnected(): Boolean = false
    override suspend fun disconnect() = Unit
}

private class NoOpVehicleApproachRepository : LmuWindowsVehicleApproachRepository {
    override fun vehicleApproachStream(): Flow<LmuWindowsVehicleApproachData> = emptyFlow()
}

private class NoOpFlagRepository : LmuWindowsFlagRepository {
    override fun flagStream(): Flow<LmuWindowsRaceFlagsData> = emptyFlow()
}

private class NoOpVehicleDamageRepository : LmuWindowsVehicleDamageRepository {
    override fun vehicleDamageStream(): Flow<LmuWindowsVehicleDamageData> = emptyFlow()
}

private class NoOpTyreCarcassTemperatureRepository : LmuWindowsTyreCarcassTemperatureRepository {
    override fun tyreCarcassTemperatureStream(): Flow<LmuWindowsTyreCarcassTemperatureData> = emptyFlow()
}
