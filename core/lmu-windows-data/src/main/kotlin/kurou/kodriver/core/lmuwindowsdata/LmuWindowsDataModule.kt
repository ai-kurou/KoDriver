package kurou.kodriver.core.lmuwindowsdata

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.core.lmuwindowsdata.datasource.SharedLmuWindowsMemorySource
import kurou.kodriver.core.lmuwindowsdata.repository.LmuWindowsRepositoryImpl
import kurou.kodriver.core.lmuwindowsdata.repository.SharedMemoryFlagRepository
import kurou.kodriver.core.lmuwindowsdata.repository.SharedMemoryProximityRepository
import kurou.kodriver.core.lmuwindowsdata.repository.SharedMemoryVehicleDamageRepository
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
    single { SharedLmuWindowsMemorySource(scope = get()) }
    single<LmuWindowsRepository> {
        if (isWindows) LmuWindowsRepositoryImpl(source = get()) else NoOpLmuWindowsRepository()
    }
    single<ProximityRepository> {
        if (isWindows) {
            SharedMemoryProximityRepository(thresholdsRepository = get(), source = get())
        } else {
            NoOpProximityRepository()
        }
    }
    single<FlagRepository> {
        if (isWindows) SharedMemoryFlagRepository(source = get()) else NoOpFlagRepository()
    }
    single<VehicleDamageRepository> {
        if (isWindows) SharedMemoryVehicleDamageRepository(source = get()) else NoOpVehicleDamageRepository()
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
