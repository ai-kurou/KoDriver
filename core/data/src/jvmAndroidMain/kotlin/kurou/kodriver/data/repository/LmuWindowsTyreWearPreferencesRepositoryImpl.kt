package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.data.model.LmuWindowsTyreWearPreferences
import kurou.kodriver.domain.repository.LmuWindowsTyreWearPreferencesRepository

internal class LmuWindowsTyreWearPreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsTyreWearPreferences>,
) : LmuWindowsTyreWearPreferencesRepository {
    override fun observeThresholdPercentage(): Flow<Int> = dataStore.observeProperty { it.thresholdPercentage }

    override suspend fun saveThresholdPercentage(percentage: Int) {
        dataStore.saveProperty(percentage) { prefs, value -> prefs.copy(thresholdPercentage = value) }
    }
}
