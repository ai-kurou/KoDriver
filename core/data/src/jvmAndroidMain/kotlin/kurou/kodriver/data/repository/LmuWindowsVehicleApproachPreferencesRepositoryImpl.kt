package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.data.model.LmuWindowsVehicleApproachPreferences
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.model.VehicleApproachSustainedReadoutType
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository

internal class LmuWindowsVehicleApproachPreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsVehicleApproachPreferences>,
) : LmuWindowsVehicleApproachPreferencesRepository {
    override fun observeSkipFirstLap(): Flow<Boolean> = dataStore.observeProperty { it.skipFirstLap }

    override suspend fun saveSkipFirstLap(skip: Boolean) {
        dataStore.saveProperty(skip) { prefs, value -> prefs.copy(skipFirstLap = value) }
    }

    override fun observeStartReadoutType(): Flow<VehicleApproachStartReadoutType> =
        dataStore.observeProperty { VehicleApproachStartReadoutType.fromId(it.startReadoutType) }

    override suspend fun saveStartReadoutType(type: VehicleApproachStartReadoutType) {
        dataStore.saveProperty(type.id) { prefs, value -> prefs.copy(startReadoutType = value) }
    }

    override fun observeSustainedReadoutType(): Flow<VehicleApproachSustainedReadoutType> =
        dataStore.observeProperty { VehicleApproachSustainedReadoutType.fromId(it.sustainedReadoutType) }

    override suspend fun saveSustainedReadoutType(type: VehicleApproachSustainedReadoutType) {
        dataStore.saveProperty(type.id) { prefs, value -> prefs.copy(sustainedReadoutType = value) }
    }

    override fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> =
        dataStore.observeProperty { prefs ->
            prefs.enabledStates
                .mapNotNull { (key, enabled) -> ReadoutItemKey.fromValue(key)?.let { it to enabled } }
                .toMap()
        }

    override suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    ) {
        dataStore.saveProperty(enabled) { prefs, value ->
            prefs.copy(
                enabledStates =
                    prefs.enabledStates + (key.value to value),
            )
        }
    }
}
