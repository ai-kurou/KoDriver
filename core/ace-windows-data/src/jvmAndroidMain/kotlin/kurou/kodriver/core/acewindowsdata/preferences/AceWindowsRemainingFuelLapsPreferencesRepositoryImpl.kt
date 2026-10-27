package kurou.kodriver.core.acewindowsdata.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsPreferencesRepository

internal class AceWindowsRemainingFuelLapsPreferencesRepositoryImpl(
    private val dataStore: DataStore<AceWindowsRemainingFuelLapsPreferences>,
) : AceWindowsRemainingFuelLapsPreferencesRepository {
    override suspend fun resetToDefault() {
        dataStore.updateData { AceWindowsRemainingFuelLapsPreferences() }
    }

    override fun observeThresholdLaps(): Flow<Int> = dataStore.observeProperty { it.thresholdLaps }

    override suspend fun saveThresholdLaps(laps: Int) {
        dataStore.saveProperty(laps) { prefs, value -> prefs.copy(thresholdLaps = value) }
    }

    override fun observeReadoutText(): Flow<String> = dataStore.observeProperty { it.readoutText }

    override suspend fun saveReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(readoutText = value) }
    }

    override fun observeEmptyReadoutText(): Flow<String> = dataStore.observeProperty { it.emptyReadoutText }

    override suspend fun saveEmptyReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(emptyReadoutText = value) }
    }
}
