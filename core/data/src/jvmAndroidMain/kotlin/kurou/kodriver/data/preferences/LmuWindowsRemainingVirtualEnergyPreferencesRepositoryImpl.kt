package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository

internal class LmuWindowsRemainingVirtualEnergyPreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsRemainingVirtualEnergyPreferences>,
) : LmuWindowsRemainingVirtualEnergyPreferencesRepository {
    override fun observeThresholdPercentage(): Flow<Int> = dataStore.observeProperty { it.thresholdPercentage }

    override suspend fun saveThresholdPercentage(percentage: Int) {
        dataStore.saveProperty(percentage) { prefs, value -> prefs.copy(thresholdPercentage = value) }
    }
}
