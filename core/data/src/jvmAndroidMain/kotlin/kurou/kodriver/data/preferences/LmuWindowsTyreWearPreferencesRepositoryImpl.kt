package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsTyreWearPreferencesRepository

internal class LmuWindowsTyreWearPreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsTyreWearPreferences>,
) : LmuWindowsTyreWearPreferencesRepository {
    override fun observeThresholdPercentage(): Flow<Int> = dataStore.observeProperty { it.thresholdPercentage }

    override suspend fun saveThresholdPercentage(percentage: Int) {
        dataStore.saveProperty(percentage) { prefs, value -> prefs.copy(thresholdPercentage = value) }
    }
}
