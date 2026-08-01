package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.model.LmuWindowsRemainingVirtualEnergyPreferences
import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository

internal class LmuWindowsRemainingVirtualEnergyPreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsRemainingVirtualEnergyPreferences>,
) : LmuWindowsRemainingVirtualEnergyPreferencesRepository {
    override fun observeThresholdPercentage(): Flow<Int> =
        dataStore.data.map { it.thresholdPercentage }

    override suspend fun saveThresholdPercentage(percentage: Int) {
        dataStore.updateData { it.copy(thresholdPercentage = percentage) }
    }
}
