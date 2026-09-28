package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsFlagReadoutTextPreferencesRepository

internal class LmuWindowsFlagReadoutTextPreferencesRepositoryImpl(
    private val dataStore: DataStore<FlagReadoutTextPreferences>,
) : LmuWindowsFlagReadoutTextPreferencesRepository {
    override fun observeSectorYellowFlagText(): Flow<String> = dataStore.observeProperty { it.sectorYellowFlagText }

    override suspend fun saveSectorYellowFlagText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(sectorYellowFlagText = value) }
    }
}
