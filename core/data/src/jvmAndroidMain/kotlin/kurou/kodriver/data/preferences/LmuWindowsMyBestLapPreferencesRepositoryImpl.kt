package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository

internal class LmuWindowsMyBestLapPreferencesRepositoryImpl(
    private val dataStore: DataStore<MyBestLapPreferences>,
) : LmuWindowsMyBestLapPreferencesRepository {
    override fun observeReadoutText(): Flow<String> = dataStore.observeProperty { it.lmuWindowsReadoutText }

    override suspend fun saveReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(lmuWindowsReadoutText = value) }
    }
}
