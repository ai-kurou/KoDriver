package kurou.kodriver.feature.lmuwindowsnarrator

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.core.narrator.SoundPlayer
import kurou.kodriver.domain.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.model.VehicleApproachSustainedReadoutType
import kurou.kodriver.domain.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsPitStatusRepository
import kurou.kodriver.domain.repository.LmuWindowsRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreWearPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreWearRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.repository.LmuWindowsVirtualEnergyRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * テスト用の Fake Koin モジュール（testFixtures）。:core:lmu-windows-data / :core:data の代わりに
 * LMU 系 Repository と SoundPlayer の Fake/No-Op 実装をバインドする。
 */
val fakeLmuWindowsNarratorModule =
    module {
        single<LmuWindowsVehicleApproachRepository> { FakeLmuWindowsVehicleApproachRepository() }
        single<LmuWindowsFlagRepository> { FakeLmuWindowsFlagRepository() }
        single<LmuWindowsRepository> { FakeLmuWindowsRepository() }
        single<LmuWindowsVehicleApproachPreferencesRepository> { FakeLmuWindowsVehicleApproachPreferencesRepository() }
        single<LmuWindowsVehicleDamagePreferencesRepository> { FakeLmuWindowsVehicleDamagePreferencesRepository() }
        single<LmuWindowsVehicleDamageRepository> { FakeLmuWindowsVehicleDamageRepository() }
        single<SoundPlayer>(named("lmu_windows")) { NoOpSoundPlayer() }
        single<SoundVolumePreferencesRepository> { FakeSoundVolumePreferencesRepository() }
        single<LmuWindowsTyreCarcassTemperatureRepository> { FakeLmuWindowsTyreCarcassTemperatureRepository() }
        single<LmuWindowsTyreTemperaturePreferencesRepository> { FakeLmuWindowsTyreTemperaturePreferencesRepository() }
        single<LmuWindowsTyreWearRepository> { FakeLmuWindowsTyreWearRepository() }
        single<LmuWindowsTyreWearPreferencesRepository> { FakeLmuWindowsTyreWearPreferencesRepository() }
        single<LmuWindowsMyBestLapPreferencesRepository> { FakeLmuWindowsMyBestLapPreferencesRepository() }
        single<LmuWindowsVirtualEnergyRepository> { FakeLmuWindowsVirtualEnergyRepository() }
        single<LmuWindowsVehicleClassRepository> { FakeLmuWindowsVehicleClassRepository() }
        single<LmuWindowsPitStatusRepository> { FakeLmuWindowsPitStatusRepository() }
    }

class FakeLmuWindowsVehicleApproachRepository : LmuWindowsVehicleApproachRepository {
    override fun vehicleApproachStream(): Flow<LmuWindowsVehicleApproachData> = emptyFlow()
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
    private val startReadoutTypeFlow = MutableStateFlow(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
    private val sustainedReadoutTypeFlow = MutableStateFlow(VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT)
    private val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())

    override fun observeSkipFirstLap(): Flow<Boolean> = skipFirstLapFlow

    override suspend fun saveSkipFirstLap(skip: Boolean) {
        skipFirstLapFlow.update { skip }
    }

    override fun observeStartReadoutType(): Flow<VehicleApproachStartReadoutType> = startReadoutTypeFlow

    override suspend fun saveStartReadoutType(type: VehicleApproachStartReadoutType) {
        startReadoutTypeFlow.update { type }
    }

    override fun observeSustainedReadoutType(): Flow<VehicleApproachSustainedReadoutType> = sustainedReadoutTypeFlow

    override suspend fun saveSustainedReadoutType(type: VehicleApproachSustainedReadoutType) {
        sustainedReadoutTypeFlow.update { type }
    }

    override fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> = enabledStatesFlow

    override suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    ) {
        enabledStatesFlow.update { it + (key to enabled) }
    }
}

class FakeLmuWindowsVehicleDamagePreferencesRepository : LmuWindowsVehicleDamagePreferencesRepository {
    override fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> = MutableStateFlow(emptyMap())

    override suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    ) = Unit
}

class FakeLmuWindowsVehicleDamageRepository : LmuWindowsVehicleDamageRepository {
    override fun vehicleDamageStream(): Flow<LmuWindowsVehicleDamageData> = emptyFlow()
}

class NoOpSoundPlayer : SoundPlayer {
    override val isPlaying: Boolean = false

    override suspend fun play(
        bytes: ByteArray,
        volume: Int,
    ) = Unit
}

class FakeSoundVolumePreferencesRepository : SoundVolumePreferencesRepository {
    private val flow = MutableStateFlow(100)

    override fun volume(): Flow<Int> = flow

    override suspend fun saveVolume(volume: Int) {
        flow.update { volume }
    }
}

class FakeLmuWindowsTyreCarcassTemperatureRepository : LmuWindowsTyreCarcassTemperatureRepository {
    override fun tyreCarcassTemperatureStream(): Flow<LmuWindowsTyreCarcassTemperatureData> = emptyFlow()
}

class FakeLmuWindowsTyreTemperaturePreferencesRepository : LmuWindowsTyreTemperaturePreferencesRepository {
    private val flow = MutableStateFlow(90)
    private val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    private val lowWarningPhasesFlow = MutableStateFlow<Map<SessionPhase, Boolean>>(emptyMap())

    override fun observeHighThresholdCelsius(): Flow<Int> = flow

    override suspend fun saveHighThresholdCelsius(celsius: Int) {
        flow.update { celsius }
    }

    override fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> = enabledStatesFlow

    override suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    ) {
        enabledStatesFlow.update { it + (key to enabled) }
    }

    override fun observeLowWarningPhases(): Flow<Map<SessionPhase, Boolean>> = lowWarningPhasesFlow

    override suspend fun saveLowWarningPhases(phases: Set<SessionPhase>) {
        lowWarningPhasesFlow.update { phases.associateWith { true } }
    }
}

class FakeLmuWindowsTyreWearRepository : LmuWindowsTyreWearRepository {
    override fun tyreWearStream(): Flow<LmuWindowsTyreWearData> = emptyFlow()
}

class FakeLmuWindowsTyreWearPreferencesRepository : LmuWindowsTyreWearPreferencesRepository {
    private val flow = MutableStateFlow(50)

    override fun observeThresholdPercentage(): Flow<Int> = flow

    override suspend fun saveThresholdPercentage(percentage: Int) {
        flow.update { percentage }
    }
}

class FakeLmuWindowsMyBestLapPreferencesRepository : LmuWindowsMyBestLapPreferencesRepository {
    private val flow = MutableStateFlow(MyBestLapVoiceType.FORMAL)

    override fun observeVoiceType(): Flow<MyBestLapVoiceType> = flow

    override suspend fun saveVoiceType(type: MyBestLapVoiceType) {
        flow.update { type }
    }
}

class FakeLmuWindowsVirtualEnergyRepository : LmuWindowsVirtualEnergyRepository {
    override fun virtualEnergyStream(): Flow<LmuWindowsVirtualEnergyData> = emptyFlow()
}

class FakeLmuWindowsVehicleClassRepository : LmuWindowsVehicleClassRepository {
    override fun vehicleClassStream(): Flow<LmuWindowsVehicleClassData> = emptyFlow()
}

class FakeLmuWindowsPitStatusRepository : LmuWindowsPitStatusRepository {
    override fun pitStatusStream(): Flow<LmuWindowsPitStatusData> = emptyFlow()
}
