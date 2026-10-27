package kurou.kodriver.feature.lmuwindowsnarrator

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kurou.kodriver.core.narrator.SoundPlayer
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_SELECTED_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_SELECTED_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_DAMAGE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_DAMAGE_PART_DETACHED_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_DAMAGE_TYRE_DETACHED_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeWearData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsCarGapData
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
import kurou.kodriver.domain.lmuwindows.model.SessionPhase
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsAllVehicleClasses
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsBrakeTemperatureRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsBrakeWearRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsCarGapRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitStatusRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreCarcassTemperatureRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreDetachedRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreTemperatureReadoutTextPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreWearPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreWearRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachReadoutTextPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamagePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleDamageRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVirtualEnergyRepository
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_START_LEFT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_START_RIGHT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_LEFT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_RIGHT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.HotkeyRepository
import kurou.kodriver.domain.repository.ManualReadoutHotkeyRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * テスト用の Fake Koin モジュール（testFixtures）。:core:lmu-windows-data / :core:data の代わりに
 * LMU 系 Repository と SoundPlayer の Fake/No-Op 実装をバインドする。
 */
val fakeLmuWindowsNarratorModule =
    module {
        single<ManualReadoutHotkeyRepository> { FakeManualReadoutHotkeyRepository() }
        single<HotkeyRepository> { FakeHotkeyRepository() }
        single<LmuWindowsVehicleApproachRepository> { FakeLmuWindowsVehicleApproachRepository() }
        single<LmuWindowsFlagRepository> { FakeLmuWindowsFlagRepository() }
        single<LmuWindowsRepository> { FakeLmuWindowsRepository() }
        single { FakeLmuWindowsVehicleApproachPreferencesRepository() }
        single<LmuWindowsVehicleApproachPreferencesRepository> {
            get<FakeLmuWindowsVehicleApproachPreferencesRepository>()
        }
        single<LmuWindowsVehicleApproachReadoutTextPreferencesRepository> {
            get<FakeLmuWindowsVehicleApproachPreferencesRepository>()
        }
        single<LmuWindowsVehicleDamagePreferencesRepository> { FakeLmuWindowsVehicleDamagePreferencesRepository() }
        single<LmuWindowsVehicleDamageRepository> { FakeLmuWindowsVehicleDamageRepository() }
        single<LmuWindowsTyreDetachedRepository> { FakeLmuWindowsTyreDetachedRepository() }
        single<LmuWindowsCarGapRepository> { FakeLmuWindowsCarGapRepository() }
        single<SoundPlayer>(named(Simulator.LmuWindows.id)) { NoOpSoundPlayer() }
        single<SoundVolumePreferencesRepository> { FakeSoundVolumePreferencesRepository() }
        single<LmuWindowsTyreCarcassTemperatureRepository> { FakeLmuWindowsTyreCarcassTemperatureRepository() }
        single<LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository> {
            FakeLmuWindowsVehicleClassBrakeTemperaturePreferencesRepository()
        }
        single<LmuWindowsBrakeTemperatureRepository> { FakeLmuWindowsBrakeTemperatureRepository() }
        single<LmuWindowsVehicleClassBrakeWearPreferencesRepository> {
            FakeLmuWindowsVehicleClassBrakeWearPreferencesRepository()
        }
        single<LmuWindowsBrakeWearRepository> { FakeLmuWindowsBrakeWearRepository() }
        single<LmuWindowsTyreTemperaturePreferencesRepository> { FakeLmuWindowsTyreTemperaturePreferencesRepository() }
        single<LmuWindowsTyreTemperatureReadoutTextPreferencesRepository> {
            FakeLmuWindowsTyreTemperatureReadoutTextPreferencesRepository()
        }
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

class FakeLmuWindowsVehicleApproachPreferencesRepository :
    LmuWindowsVehicleApproachPreferencesRepository,
    LmuWindowsVehicleApproachReadoutTextPreferencesRepository {
    override suspend fun resetToDefault() = Unit

    private val skipFirstLapFlow = MutableStateFlow(true)
    private val startLeftReadoutTextFlow =
        MutableStateFlow(LMU_WINDOWS_VEHICLE_APPROACH_START_LEFT_READOUT_TEXT_DEFAULT)
    private val startRightReadoutTextFlow =
        MutableStateFlow(LMU_WINDOWS_VEHICLE_APPROACH_START_RIGHT_READOUT_TEXT_DEFAULT)
    private val sustainedLeftReadoutTextFlow =
        MutableStateFlow(LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_LEFT_READOUT_TEXT_DEFAULT)
    private val sustainedRightReadoutTextFlow =
        MutableStateFlow(LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_RIGHT_READOUT_TEXT_DEFAULT)
    private val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())

    override fun observeSkipFirstLap(): Flow<Boolean> = skipFirstLapFlow

    override suspend fun saveSkipFirstLap(skip: Boolean) {
        skipFirstLapFlow.update { skip }
    }

    override fun observeStartLeftReadoutText(): Flow<String> = startLeftReadoutTextFlow

    override suspend fun saveStartLeftReadoutText(text: String) {
        startLeftReadoutTextFlow.update { text }
    }

    override fun observeStartRightReadoutText(): Flow<String> = startRightReadoutTextFlow

    override suspend fun saveStartRightReadoutText(text: String) {
        startRightReadoutTextFlow.update { text }
    }

    override fun observeSustainedLeftReadoutText(): Flow<String> = sustainedLeftReadoutTextFlow

    override suspend fun saveSustainedLeftReadoutText(text: String) {
        sustainedLeftReadoutTextFlow.update { text }
    }

    override fun observeSustainedRightReadoutText(): Flow<String> = sustainedRightReadoutTextFlow

    override suspend fun saveSustainedRightReadoutText(text: String) {
        sustainedRightReadoutTextFlow.update { text }
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
    override suspend fun resetToDefault() = Unit

    override fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> = MutableStateFlow(emptyMap())

    override suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    ) = Unit

    private val overheatText = MutableStateFlow(LMU_WINDOWS_VEHICLE_DAMAGE_OVERHEAT_READOUT_TEXT_DEFAULT)

    override fun observeOverheatReadoutText(): Flow<String> = overheatText

    override suspend fun saveOverheatReadoutText(text: String) {
        overheatText.update { text }
    }

    private val partDetachedText = MutableStateFlow(LMU_WINDOWS_VEHICLE_DAMAGE_PART_DETACHED_READOUT_TEXT_DEFAULT)

    override fun observePartDetachedReadoutText(): Flow<String> = partDetachedText

    override suspend fun savePartDetachedReadoutText(text: String) {
        partDetachedText.update { text }
    }

    private val tyreDetachedText = MutableStateFlow(LMU_WINDOWS_VEHICLE_DAMAGE_TYRE_DETACHED_READOUT_TEXT_DEFAULT)

    override fun observeTyreDetachedReadoutText(): Flow<String> = tyreDetachedText

    override suspend fun saveTyreDetachedReadoutText(text: String) {
        tyreDetachedText.update { text }
    }
}

class FakeLmuWindowsVehicleDamageRepository : LmuWindowsVehicleDamageRepository {
    override fun vehicleDamageStream(): Flow<LmuWindowsVehicleDamageData> = emptyFlow()
}

class FakeLmuWindowsTyreDetachedRepository : LmuWindowsTyreDetachedRepository {
    override fun tyreDetachedStream(): Flow<LmuWindowsTyreDetachedData> = emptyFlow()
}

class FakeLmuWindowsCarGapRepository : LmuWindowsCarGapRepository {
    override fun carGapStream(): Flow<LmuWindowsCarGapData> = emptyFlow()
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

class FakeLmuWindowsBrakeWearRepository : LmuWindowsBrakeWearRepository {
    override fun brakeWearStream(): Flow<LmuWindowsBrakeWearData?> = emptyFlow()
}

class FakeLmuWindowsBrakeTemperatureRepository : LmuWindowsBrakeTemperatureRepository {
    override fun brakeTemperatureStream(): Flow<LmuWindowsBrakeTemperatureData> = emptyFlow()
}

class FakeLmuWindowsTyreTemperaturePreferencesRepository : LmuWindowsTyreTemperaturePreferencesRepository {
    override suspend fun resetToDefault() = Unit

    private val enabledStatesFlow = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    private val lowWarningPhasesFlow = MutableStateFlow<Map<SessionPhase, Boolean>>(emptyMap())

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

class FakeLmuWindowsTyreTemperatureReadoutTextPreferencesRepository :
    LmuWindowsTyreTemperatureReadoutTextPreferencesRepository {
    private val overheatReadoutTextFlow = MutableStateFlow(LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT)
    private val coldReadoutTextFlow = MutableStateFlow(LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT)

    override fun observeOverheatReadoutText(): Flow<String> = overheatReadoutTextFlow

    override suspend fun saveOverheatReadoutText(text: String) {
        overheatReadoutTextFlow.update { text }
    }

    override fun observeColdReadoutText(): Flow<String> = coldReadoutTextFlow

    override suspend fun saveColdReadoutText(text: String) {
        coldReadoutTextFlow.update { text }
    }
}

class FakeLmuWindowsTyreWearRepository : LmuWindowsTyreWearRepository {
    override fun tyreWearStream(): Flow<LmuWindowsTyreWearData> = emptyFlow()
}

class FakeLmuWindowsTyreWearPreferencesRepository : LmuWindowsTyreWearPreferencesRepository {
    override suspend fun resetToDefault() = Unit

    private val flow = MutableStateFlow(50)
    private val text = MutableStateFlow(LMU_WINDOWS_TYRE_WEAR_READOUT_TEXT_DEFAULT)

    override fun observeReadoutText(): Flow<String> = text

    override suspend fun saveReadoutText(text: String) {
        this.text.update { text }
    }

    override fun observeThresholdPercentage(): Flow<Int> = flow

    override suspend fun saveThresholdPercentage(percentage: Int) {
        flow.update { percentage }
    }
}

class FakeLmuWindowsMyBestLapPreferencesRepository : LmuWindowsMyBestLapPreferencesRepository {
    override suspend fun resetToDefault() = Unit

    private val flow = MutableStateFlow(LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT)

    override fun observeReadoutText(): Flow<String> = flow

    override suspend fun saveReadoutText(text: String) {
        flow.update { text }
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

class FakeLmuWindowsVehicleClassBrakeTemperaturePreferencesRepository :
    LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository {
    override suspend fun resetToDefault() = Unit

    private val thresholds =
        MutableStateFlow(
            lmuWindowsAllVehicleClasses.associateWith {
                lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(it)
            },
        )
    private val selected = MutableStateFlow(LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_SELECTED_DEFAULT)
    private val text = MutableStateFlow(LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT)

    override fun observeHighThresholdCelsius(): Flow<Map<LmuWindowsVehicleClassData, Int>> = thresholds

    override suspend fun saveHighThresholdCelsius(
        vehicleClass: LmuWindowsVehicleClassData,
        celsius: Int,
    ) {
        thresholds.update { it + (vehicleClass to celsius) }
    }

    override fun observeSelectedVehicleClass(): Flow<LmuWindowsVehicleClassData> = selected

    override suspend fun saveSelectedVehicleClass(vehicleClass: LmuWindowsVehicleClassData) {
        selected.update { vehicleClass }
    }

    override fun observeReadoutText(): Flow<String> = text

    override suspend fun saveReadoutText(text: String) {
        this.text.update { text }
    }
}

class FakeLmuWindowsVehicleClassBrakeWearPreferencesRepository : LmuWindowsVehicleClassBrakeWearPreferencesRepository {
    override suspend fun resetToDefault() = Unit

    private val thresholds =
        MutableStateFlow(
            lmuWindowsAllVehicleClasses.associateWith {
                lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault(it)
            },
        )
    private val selected = MutableStateFlow(LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_SELECTED_DEFAULT)
    private val text = MutableStateFlow(LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT)

    override fun observeLowThresholdPercent(): Flow<Map<LmuWindowsVehicleClassData, Int>> = thresholds

    override suspend fun saveLowThresholdPercent(
        vehicleClass: LmuWindowsVehicleClassData,
        percent: Int,
    ) {
        thresholds.update { it + (vehicleClass to percent) }
    }

    override fun observeSelectedVehicleClass(): Flow<LmuWindowsVehicleClassData> = selected

    override suspend fun saveSelectedVehicleClass(vehicleClass: LmuWindowsVehicleClassData) {
        selected.update { vehicleClass }
    }

    override fun observeReadoutText(): Flow<String> = text

    override suspend fun saveReadoutText(text: String) {
        this.text.update { text }
    }
}

class FakeHotkeyRepository : HotkeyRepository {
    override suspend fun retryRegistration() = Unit

    override fun registrationStateStream(): Flow<HotkeyRegistrationState> = flowOf(HotkeyRegistrationState.Unsupported)

    override fun hotkeyPressStream(): Flow<Unit> = emptyFlow()
}

class FakeManualReadoutHotkeyRepository : ManualReadoutHotkeyRepository {
    override fun pressStream(): Flow<ManualReadoutRequest> = emptyFlow()

    override fun registrationStateStream(
        simulatorId: String,
        setId: String,
    ): Flow<HotkeyRegistrationState> = flowOf(HotkeyRegistrationState.Unsupported)

    override suspend fun retryRegistration(
        simulatorId: String,
        setId: String,
    ) = Unit
}
