package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.model.AceWindowsRemainingFuelPreferences
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelPreferencesRepository

internal class AceWindowsRemainingFuelPreferencesRepositoryImpl(
    private val dataStore: DataStore<AceWindowsRemainingFuelPreferences>,
) : AceWindowsRemainingFuelPreferencesRepository {

    override fun observeThresholdPercentage(): Flow<Int> =
        dataStore.data.map { it.thresholdPercentage }

    override suspend fun saveThresholdPercentage(percentage: Int) {
        dataStore.updateData { it.copy(thresholdPercentage = percentage) }
    }
}
