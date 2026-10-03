package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachReadoutTextPreferencesRepository

internal class LmuWindowsVehicleApproachReadoutTextPreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsVehicleApproachPreferences>,
) : LmuWindowsVehicleApproachReadoutTextPreferencesRepository {
    override fun observeStartLeftReadoutText(): Flow<String> = dataStore.observeProperty { it.startLeftReadoutText }

    override suspend fun saveStartLeftReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(startLeftReadoutText = value) }
    }

    override fun observeStartRightReadoutText(): Flow<String> = dataStore.observeProperty { it.startRightReadoutText }

    override suspend fun saveStartRightReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(startRightReadoutText = value) }
    }

    override fun observeSustainedLeftReadoutText(): Flow<String> =
        dataStore.observeProperty { it.sustainedLeftReadoutText }

    override suspend fun saveSustainedLeftReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(sustainedLeftReadoutText = value) }
    }

    override fun observeSustainedRightReadoutText(): Flow<String> =
        dataStore.observeProperty { it.sustainedRightReadoutText }

    override suspend fun saveSustainedRightReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(sustainedRightReadoutText = value) }
    }
}
