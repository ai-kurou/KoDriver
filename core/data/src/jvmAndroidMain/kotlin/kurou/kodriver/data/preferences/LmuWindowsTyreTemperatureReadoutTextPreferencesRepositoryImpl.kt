package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperatureReadoutTextPreferencesRepository

internal class LmuWindowsTyreTemperatureReadoutTextPreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsTyreTemperaturePreferences>,
) : LmuWindowsTyreTemperatureReadoutTextPreferencesRepository {
    override fun observeOverheatReadoutText(): Flow<String> = dataStore.observeProperty { it.overheatReadoutText }

    override suspend fun saveOverheatReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(overheatReadoutText = value) }
    }

    override fun observeColdReadoutText(): Flow<String> = dataStore.observeProperty { it.coldReadoutText }

    override suspend fun saveColdReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(coldReadoutText = value) }
    }
}
