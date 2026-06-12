package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.model.VehicleDamagePreferences
import kurou.kodriver.domain.repository.VehicleDamagePreferencesRepository

internal class VehicleDamagePreferencesRepositoryImpl(
    private val dataStore: DataStore<VehicleDamagePreferences>,
) : VehicleDamagePreferencesRepository {

    override fun observeEnabledStates(): Flow<Map<String, Boolean>> =
        dataStore.data.map { it.enabledStates }

    override suspend fun saveEnabledState(key: String, enabled: Boolean) {
        dataStore.updateData { it.copy(enabledStates = it.enabledStates + (key to enabled)) }
    }
}
