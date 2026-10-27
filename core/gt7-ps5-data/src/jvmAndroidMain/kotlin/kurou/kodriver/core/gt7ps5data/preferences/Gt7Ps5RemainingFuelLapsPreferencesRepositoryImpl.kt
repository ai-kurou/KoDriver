package kurou.kodriver.core.gt7ps5data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository

internal class Gt7Ps5RemainingFuelLapsPreferencesRepositoryImpl(
    private val dataStore: DataStore<Gt7Ps5RemainingFuelLapsPreferences>,
) : Gt7Ps5RemainingFuelLapsPreferencesRepository {
    override suspend fun resetToDefault() {
        dataStore.updateData { Gt7Ps5RemainingFuelLapsPreferences() }
    }

    override fun observeRemainingFuelLaps(): Flow<Int> = dataStore.observeProperty { it.remainingFuelLaps }

    override suspend fun saveRemainingFuelLaps(laps: Int) {
        dataStore.saveProperty(laps) { prefs, value -> prefs.copy(remainingFuelLaps = value) }
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
