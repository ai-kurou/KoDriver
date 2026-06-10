package kurou.kodriver.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.data.datasource.SharedLmuMemorySource
import kurou.kodriver.data.repository.LmuRepositoryImpl
import kurou.kodriver.data.repository.SharedMemoryFlagRepository
import kurou.kodriver.data.repository.SharedMemoryProximityRepository
import kurou.kodriver.domain.model.LmuTelemetryData
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.repository.FlagRepository
import kurou.kodriver.domain.repository.LmuRepository
import kurou.kodriver.domain.repository.ProximityRepository
import kurou.kodriver.domain.repository.ProximityThresholdsRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import org.koin.dsl.module

private val kodriverDirectory = "${System.getProperty("user.home")}/.kodriver"
private val isWindows = System.getProperty("os.name").lowercase().startsWith("windows")

val desktopDataModule = module {
    single { CoroutineScope(SupervisorJob()) }
    single {
        SharedLmuMemorySource(scope = get())
    }
    single<LmuRepository> {
        if (isWindows) LmuRepositoryImpl(source = get()) else NoOpLmuRepository()
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
    single<SimulatorPreferencesRepository> {
        createSimulatorPreferencesRepository(directory = kodriverDirectory)
    }
    single<ReadoutPreferencesRepository> {
        createReadoutPreferencesRepository(directory = kodriverDirectory)
    }
    single<ProximityThresholdsRepository> {
        createProximityThresholdsRepository(directory = kodriverDirectory)
    }
}

private class NoOpLmuRepository : LmuRepository {
    override fun telemetryStream(): Flow<LmuTelemetryData> = emptyFlow()
    override suspend fun isConnected(): Boolean = false
    override suspend fun disconnect() = Unit
}

private class NoOpProximityRepository : ProximityRepository {
    override fun proximityStream(): Flow<ProximityData> = emptyFlow()
}

private class NoOpFlagRepository : FlagRepository {
    override fun flagStream(): Flow<RaceFlagsData> = emptyFlow()
}
