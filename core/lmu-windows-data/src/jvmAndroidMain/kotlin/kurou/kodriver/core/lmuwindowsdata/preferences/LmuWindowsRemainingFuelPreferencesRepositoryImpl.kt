package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingFuelPreferencesRepository

internal class LmuWindowsRemainingFuelPreferencesRepositoryImpl(
    private val dataStore: DataStore<LmuWindowsRemainingFuelPreferences>,
) : LmuWindowsRemainingFuelPreferencesRepository {
    override suspend fun resetToDefault() {
        dataStore.updateData { LmuWindowsRemainingFuelPreferences() }
    }

    override fun observeThresholdPercentage(): Flow<Int> = dataStore.observeProperty { it.thresholdPercentage }

    override suspend fun saveThresholdPercentage(percentage: Int) {
        dataStore.saveProperty(percentage) { prefs, item -> prefs.copy(thresholdPercentage = item) }
    }

    override fun observeReadoutText(): Flow<String> = dataStore.observeProperty { it.readoutText }

    override suspend fun saveReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, item -> prefs.copy(readoutText = item) }
    }
}
