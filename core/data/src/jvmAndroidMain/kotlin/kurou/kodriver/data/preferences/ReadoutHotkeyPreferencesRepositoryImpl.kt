package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.ReadoutHotkeyPreferencesRepository

internal class ReadoutHotkeyPreferencesRepositoryImpl(
    private val dataStore: DataStore<ReadoutHotkeyPreferences>,
) : ReadoutHotkeyPreferencesRepository {
    override fun observeVirtualKeyCode(): Flow<Int> =
        dataStore.observeProperty { prefs ->
            prefs.virtualKeyCode
        }

    override suspend fun saveVirtualKeyCode(virtualKeyCode: Int) {
        dataStore.saveProperty(virtualKeyCode) { prefs, value -> prefs.copy(virtualKeyCode = value) }
    }
}
