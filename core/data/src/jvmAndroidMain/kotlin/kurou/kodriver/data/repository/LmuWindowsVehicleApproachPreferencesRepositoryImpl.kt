package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.model.LmuWindowsVehicleApproachPreferences
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository

internal class LmuWindowsVehicleApproachPreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsVehicleApproachPreferences>,
) : LmuWindowsVehicleApproachPreferencesRepository {

    override fun observeSkipFirstLap(): Flow<Boolean> =
        dataStore.data.map { it.skipFirstLap }

    override suspend fun saveSkipFirstLap(skip: Boolean) {
        dataStore.updateData { it.copy(skipFirstLap = skip) }
    }

    override fun observeStartReadoutEnabled(): Flow<Boolean> =
        dataStore.data.map { it.startReadoutEnabled }

    override suspend fun saveStartReadoutEnabled(enabled: Boolean) {
        dataStore.updateData { it.copy(startReadoutEnabled = enabled) }
    }

    override fun observeStartReadoutType(): Flow<VehicleApproachStartReadoutType> =
        dataStore.data.map { VehicleApproachStartReadoutType.fromId(it.startReadoutType) }

    override suspend fun saveStartReadoutType(type: VehicleApproachStartReadoutType) {
        dataStore.updateData { it.copy(startReadoutType = type.id) }
    }

    override fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> =
        dataStore.data.map { prefs ->
            prefs.enabledStates
                .mapNotNull { (key, enabled) -> ReadoutItemKey.fromValue(key)?.let { it to enabled } }
                .toMap()
        }

    override suspend fun saveEnabledState(key: ReadoutItemKey, enabled: Boolean) {
        dataStore.updateData { it.copy(enabledStates = it.enabledStates + (key.value to enabled)) }
    }
}
