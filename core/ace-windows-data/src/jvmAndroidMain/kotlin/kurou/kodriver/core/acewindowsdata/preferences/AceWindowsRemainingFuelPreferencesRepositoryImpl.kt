package kurou.kodriver.core.acewindowsdata.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelPreferencesRepository

internal class AceWindowsRemainingFuelPreferencesRepositoryImpl(
    private val dataStore: DataStore<AceWindowsRemainingFuelPreferences>,
) : AceWindowsRemainingFuelPreferencesRepository {
    override suspend fun resetToDefault() {
        dataStore.updateData { AceWindowsRemainingFuelPreferences() }
    }

    override fun observeThresholdPercentage(): Flow<Int> = dataStore.observeProperty { it.thresholdPercentage }

    override suspend fun saveThresholdPercentage(percentage: Int) {
        dataStore.saveProperty(percentage) { prefs, value -> prefs.copy(thresholdPercentage = value) }
    }

    override fun observeReadoutText(): Flow<String> = dataStore.observeProperty { it.readoutText }

    override suspend fun saveReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(readoutText = value) }
    }
}
