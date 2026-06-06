package kurou.kodriver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.data.repository.LmuRepositoryImpl
import kurou.kodriver.data.repository.SharedMemoryProximityRepository
import kurou.kodriver.domain.model.LmuTelemetryData
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.repository.LmuRepository
import kurou.kodriver.domain.repository.ProximityRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import org.koin.dsl.module

private val kodriverDirectory = "${System.getProperty("user.home")}/.kodriver"
private val isWindows = System.getProperty("os.name").lowercase().startsWith("windows")

internal val desktopDataModule = module {
    single<LmuRepository> {
        if (isWindows) LmuRepositoryImpl() else NoOpLmuRepository()
    }
    single<ProximityRepository> {
        if (isWindows) SharedMemoryProximityRepository() else NoOpProximityRepository()
    }
    single<SimulatorPreferencesRepository> {
        createSimulatorPreferencesRepository(directory = kodriverDirectory)
    }
    single<ReadoutPreferencesRepository> {
        createReadoutPreferencesRepository(directory = kodriverDirectory)
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
