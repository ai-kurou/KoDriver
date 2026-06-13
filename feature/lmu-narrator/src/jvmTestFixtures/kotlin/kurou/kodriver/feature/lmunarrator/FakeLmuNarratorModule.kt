package kurou.kodriver.feature.lmunarrator

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.domain.model.LmuTelemetryData
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.model.RaceFlagsData
import kurou.kodriver.domain.model.VehicleDamageData
import kurou.kodriver.domain.repository.FlagRepository
import kurou.kodriver.domain.repository.LmuRepository
import kurou.kodriver.domain.repository.ProximityRepository
import kurou.kodriver.domain.repository.VehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.VehicleDamagePreferencesRepository
import kurou.kodriver.domain.repository.VehicleDamageRepository
import org.koin.dsl.module

val fakeLmuNarratorModule = module {
    single<ProximityRepository> { FakeProximityRepository() }
    single<FlagRepository> { FakeFlagRepository() }
    single<LmuRepository> { FakeLmuRepository() }
    single<VehicleApproachPreferencesRepository> { FakeVehicleApproachPreferencesRepository() }
    single<VehicleDamagePreferencesRepository> { FakeVehicleDamagePreferencesRepository() }
    single<VehicleDamageRepository> { FakeVehicleDamageRepository() }
    single<SoundPlayer> { NoOpSoundPlayer() }
}

class FakeProximityRepository : ProximityRepository {
    override fun proximityStream(): Flow<ProximityData> = emptyFlow()
}

class FakeFlagRepository : FlagRepository {
    override fun flagStream(): Flow<RaceFlagsData> = emptyFlow()
}

class FakeLmuRepository : LmuRepository {
    override fun telemetryStream(): Flow<LmuTelemetryData> = emptyFlow()
    override suspend fun isConnected(): Boolean = false
    override suspend fun disconnect() = Unit
}

class FakeVehicleApproachPreferencesRepository : VehicleApproachPreferencesRepository {
    private val skipFirstLapFlow = MutableStateFlow(true)
    override fun observeSkipFirstLap(): Flow<Boolean> = skipFirstLapFlow
    override suspend fun saveSkipFirstLap(skip: Boolean) { skipFirstLapFlow.value = skip }
}

class FakeVehicleDamagePreferencesRepository : VehicleDamagePreferencesRepository {
    override fun observeEnabledStates(): Flow<Map<String, Boolean>> = MutableStateFlow(emptyMap())
    override suspend fun saveEnabledState(key: String, enabled: Boolean) = Unit
}

class FakeVehicleDamageRepository : VehicleDamageRepository {
    override fun vehicleDamageStream(): Flow<VehicleDamageData> = emptyFlow()
}

class NoOpSoundPlayer : SoundPlayer {
    override val isPlaying: Boolean = false
    override suspend fun play(bytes: ByteArray) = Unit
}
