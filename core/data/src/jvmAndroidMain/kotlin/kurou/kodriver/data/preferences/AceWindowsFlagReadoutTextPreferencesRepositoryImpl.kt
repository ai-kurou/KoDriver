package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.AceWindowsFlagReadoutTextPreferencesRepository

internal class AceWindowsFlagReadoutTextPreferencesRepositoryImpl(
    private val dataStore: DataStore<AceWindowsFlagReadoutTextPreferences>,
) : AceWindowsFlagReadoutTextPreferencesRepository {
    override fun observeCheckeredFlagText(): Flow<String> = dataStore.observeProperty { it.checkeredFlagText }

    override suspend fun saveCheckeredFlagText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(checkeredFlagText = value) }
    }
}
