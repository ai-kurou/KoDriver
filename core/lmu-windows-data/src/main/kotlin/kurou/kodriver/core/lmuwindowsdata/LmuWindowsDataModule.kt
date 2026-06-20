package kurou.kodriver.core.lmuwindowsdata

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.core.lmuwindowsdata.repository.LmuWindowsFlagRepository
import kurou.kodriver.core.lmuwindowsdata.repository.LmuWindowsProximityRepository
import kurou.kodriver.core.lmuwindowsdata.repository.LmuWindowsRepositoryImpl
import kurou.kodriver.core.lmuwindowsdata.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.model.VehicleDamageData
import kurou.kodriver.domain.repository.FlagRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.ProximityRepository
import kurou.kodriver.domain.repository.VehicleDamageRepository
import org.koin.dsl.module

private val isWindows = System.getProperty("os.name").lowercase().startsWith("windows")

val lmuWindowsDataModule = module {
    single { CoroutineScope(SupervisorJob()) }
    single { LmuWindowsSharedMemorySource(scope = get()) }
    single<LmuWindowsRepository> {
        if (isWindows) LmuWindowsRepositoryImpl(source = get()) else NoOpLmuWindowsRepository()
    }
    single<ProximityRepository> {
        if (isWindows) {
            LmuWindowsProximityRepository(thresholdsRepository = get(), source = get())
        } else {
            NoOpProximityRepository()
        }
    }
    single<FlagRepository> {
        if (isWindows) LmuWindowsFlagRepository(source = get()) else NoOpFlagRepository()
    }
    single<VehicleDamageRepository> {
        if (isWindows) LmuWindowsVehicleDamageRepository(source = get()) else NoOpVehicleDamageRepository()
    }
}

private class NoOpLmuWindowsRepository : LmuWindowsRepository {
    override fun telemetryStream(): Flow<LmuWindowsTelemetryData> = emptyFlow()
    override suspend fun isConnected(): Boolean = false
    override suspend fun disconnect() = Unit
}

private class NoOpProximityRepository : ProximityRepository {
    override fun proximityStream(): Flow<ProximityData> = emptyFlow()
}

private class NoOpFlagRepository : FlagRepository {
    override fun flagStream(): Flow<RaceFlagsData> = emptyFlow()
}

private class NoOpVehicleDamageRepository : VehicleDamageRepository {
    override fun vehicleDamageStream(): Flow<VehicleDamageData> = emptyFlow()
}
