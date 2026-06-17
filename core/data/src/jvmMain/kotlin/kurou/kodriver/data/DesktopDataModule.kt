package kurou.kodriver.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.data.datasource.SharedLmuWindowsMemorySource
import kurou.kodriver.data.repository.LmuWindowsRepositoryImpl
import kurou.kodriver.data.repository.SharedMemoryFlagRepository
import kurou.kodriver.data.repository.SharedMemoryProximityRepository
import kurou.kodriver.data.repository.SharedMemoryVehicleDamageRepository
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.model.VehicleDamageData
import kurou.kodriver.domain.repository.FlagPreferencesRepository
import kurou.kodriver.domain.repository.FlagRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.ProximityRepository
import kurou.kodriver.domain.repository.ProximityThresholdsRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.ServerConnectionRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumeRepository
import kurou.kodriver.domain.repository.VehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.VehicleDamagePreferencesRepository
import kurou.kodriver.domain.repository.VehicleDamageRepository
import org.koin.dsl.module

private val kodriverDirectory = "${System.getProperty("user.home")}/.kodriver"
private val isWindows = System.getProperty("os.name").lowercase().startsWith("windows")

val desktopDataModule = module {
    single { CoroutineScope(SupervisorJob()) }
    single {
        SharedLmuWindowsMemorySource(scope = get())
    }
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
    single<SimulatorPreferencesRepository> {
        createSimulatorPreferencesRepository(directory = kodriverDirectory)
    }
    single<ReadoutPreferencesRepository> {
        createReadoutPreferencesRepository(directory = kodriverDirectory)
    }
    single<ProximityThresholdsRepository> {
        createProximityThresholdsRepository(directory = kodriverDirectory)
    }
    single<FlagPreferencesRepository> {
        createFlagPreferencesRepository(directory = kodriverDirectory)
    }
    single<VehicleApproachPreferencesRepository> {
        createVehicleApproachPreferencesRepository(directory = kodriverDirectory)
    }
    single<VehicleDamagePreferencesRepository> {
        createVehicleDamagePreferencesRepository(directory = kodriverDirectory)
    }
    single<SoundVolumeRepository> {
        createSoundVolumeRepository(directory = kodriverDirectory)
    }
    single<ServerConnectionRepository> { NoOpServerConnectionRepository() }
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

private class NoOpServerConnectionRepository : ServerConnectionRepository {
    override suspend fun isConnected(ip: String): Boolean = false
}
