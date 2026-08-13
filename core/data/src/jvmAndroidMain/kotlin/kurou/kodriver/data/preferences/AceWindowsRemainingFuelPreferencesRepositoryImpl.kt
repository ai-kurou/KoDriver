package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelPreferencesRepository

internal class AceWindowsRemainingFuelPreferencesRepositoryImpl(
    private val dataStore: DataStore<AceWindowsRemainingFuelPreferences>,
) : AceWindowsRemainingFuelPreferencesRepository {
    override fun observeThresholdPercentage(): Flow<Int> = dataStore.observeProperty { it.thresholdPercentage }

    override suspend fun saveThresholdPercentage(percentage: Int) {
        dataStore.saveProperty(percentage) { prefs, value -> prefs.copy(thresholdPercentage = value) }
    }
}
