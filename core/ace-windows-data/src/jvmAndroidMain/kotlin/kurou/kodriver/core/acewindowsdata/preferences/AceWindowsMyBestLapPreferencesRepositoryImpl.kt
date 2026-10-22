package kurou.kodriver.core.acewindowsdata.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.repository.AceWindowsMyBestLapPreferencesRepository

internal class AceWindowsMyBestLapPreferencesRepositoryImpl(
    private val dataStore: DataStore<MyBestLapPreferences>,
) : AceWindowsMyBestLapPreferencesRepository {
    override fun observeReadoutText(): Flow<String> = dataStore.observeProperty { it.aceWindowsReadoutText }

    override suspend fun saveReadoutText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(aceWindowsReadoutText = value) }
    }
}
