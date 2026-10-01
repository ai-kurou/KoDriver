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

    override fun observeBlueFlagText(): Flow<String> = dataStore.observeProperty { it.blueFlagText }

    override suspend fun saveBlueFlagText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(blueFlagText = value) }
    }

    override fun observeFullCourseYellowFlagText(): Flow<String> =
        dataStore.observeProperty { it.fullCourseYellowFlagText }

    override suspend fun saveFullCourseYellowFlagText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(fullCourseYellowFlagText = value) }
    }

    override fun observeRedFlagText(): Flow<String> = dataStore.observeProperty { it.redFlagText }

    override suspend fun saveRedFlagText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(redFlagText = value) }
    }
}
