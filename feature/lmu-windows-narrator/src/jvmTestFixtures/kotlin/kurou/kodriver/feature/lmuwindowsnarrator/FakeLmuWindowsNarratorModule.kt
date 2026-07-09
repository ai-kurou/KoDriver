package kurou.kodriver.feature.lmuwindowsnarrator

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.LmuWindowsProximityData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.repository.LmuWindowsProximityRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import org.koin.dsl.module

val fakeLmuWindowsNarratorModule = module {
    single<LmuWindowsProximityRepository> { FakeLmuWindowsProximityRepository() }
    single<LmuWindowsFlagRepository> { FakeLmuWindowsFlagRepository() }
    single<LmuWindowsRepository> { FakeLmuWindowsRepository() }
    single<LmuWindowsVehicleApproachPreferencesRepository> { FakeLmuWindowsVehicleApproachPreferencesRepository() }
    single<LmuWindowsVehicleDamagePreferencesRepository> { FakeLmuWindowsVehicleDamagePreferencesRepository() }
    single<LmuWindowsVehicleDamageRepository> { FakeLmuWindowsVehicleDamageRepository() }
    single<SoundPlayer> { NoOpSoundPlayer() }
    single<SoundVolumePreferencesRepository> { FakeSoundVolumePreferencesRepository() }
    single<LmuWindowsTyreCarcassTemperatureRepository> { FakeLmuWindowsTyreCarcassTemperatureRepository() }
    single<LmuWindowsTyreTemperaturePreferencesRepository> { FakeLmuWindowsTyreTemperaturePreferencesRepository() }
}

class FakeLmuWindowsProximityRepository : LmuWindowsProximityRepository {
    override fun proximityStream(): Flow<LmuWindowsProximityData> = emptyFlow()
}

class FakeLmuWindowsFlagRepository : LmuWindowsFlagRepository {
    override fun flagStream(): Flow<LmuWindowsRaceFlagsData> = emptyFlow()
}

class FakeLmuWindowsRepository : LmuWindowsRepository {
    override fun telemetryStream(): Flow<LmuWindowsTelemetryData> = emptyFlow()
    override suspend fun isConnected(): Boolean = false
    override suspend fun disconnect() = Unit
}

class FakeLmuWindowsVehicleApproachPreferencesRepository : LmuWindowsVehicleApproachPreferencesRepository {
    private val skipFirstLapFlow = MutableStateFlow(true)
    private val startReadoutEnabledFlow = MutableStateFlow(true)
    private val startReadoutTypeFlow = MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
    override fun observeSkipFirstLap(): Flow<Boolean> = skipFirstLapFlow
    override suspend fun saveSkipFirstLap(skip: Boolean) { skipFirstLapFlow.update { skip } }
    override fun observeStartReadoutEnabled(): Flow<Boolean> = startReadoutEnabledFlow
    override suspend fun saveStartReadoutEnabled(enabled: Boolean) { startReadoutEnabledFlow.update { enabled } }
    override fun observeStartReadoutType(): Flow<VehicleApproachStartReadoutType> = startReadoutTypeFlow
    override suspend fun saveStartReadoutType(type: VehicleApproachStartReadoutType) {
        startReadoutTypeFlow.update { type }
    }
}

class FakeLmuWindowsVehicleDamagePreferencesRepository : LmuWindowsVehicleDamagePreferencesRepository {
    override fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> = MutableStateFlow(emptyMap())
    override suspend fun saveEnabledState(key: ReadoutItemKey, enabled: Boolean) = Unit
}

class FakeLmuWindowsVehicleDamageRepository : LmuWindowsVehicleDamageRepository {
    override fun vehicleDamageStream(): Flow<LmuWindowsVehicleDamageData> = emptyFlow()
}

class NoOpSoundPlayer : SoundPlayer {
    override val isPlaying: Boolean = false
    override suspend fun play(bytes: ByteArray, volume: Int) = Unit
}

class FakeSoundVolumePreferencesRepository : SoundVolumePreferencesRepository {
    private val flow = MutableStateFlow(100)
    override fun volume(): Flow<Int> = flow
    override suspend fun saveVolume(volume: Int) { flow.update { volume } }
}

class FakeLmuWindowsTyreCarcassTemperatureRepository : LmuWindowsTyreCarcassTemperatureRepository {
    override fun tyreCarcassTemperatureStream(): Flow<LmuWindowsTyreCarcassTemperatureData> = emptyFlow()
}

class FakeLmuWindowsTyreTemperaturePreferencesRepository : LmuWindowsTyreTemperaturePreferencesRepository {
    private val flow = MutableStateFlow(90)
    private val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    override fun observeHighThresholdCelsius(): Flow<Int> = flow
    override suspend fun saveHighThresholdCelsius(celsius: Int) { flow.update { celsius } }
    override fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> = enabledStatesFlow
    override suspend fun saveEnabledState(key: ReadoutItemKey, enabled: Boolean) {
        enabledStatesFlow.update { it + (key to enabled) }
    }
}
