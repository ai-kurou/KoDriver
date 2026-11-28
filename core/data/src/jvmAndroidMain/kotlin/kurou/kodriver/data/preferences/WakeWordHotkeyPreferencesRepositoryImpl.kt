package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.speechrecognition.model.WakeWordHotkey
import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyPreferencesRepository

internal class WakeWordHotkeyPreferencesRepositoryImpl(
    private val dataStore: DataStore<WakeWordHotkeyPreferences>,
) : WakeWordHotkeyPreferencesRepository {
    override fun observeWakeWordHotkey(): Flow<WakeWordHotkey> =
        dataStore.observeProperty {
            WakeWordHotkey(it.virtualKeyCode, ManualReadoutHotkeyModifiers.fromValue(it.modifiers))
        }

    override suspend fun saveWakeWordHotkey(hotkey: WakeWordHotkey) {
        dataStore.saveProperty(hotkey) { prefs, value ->
            prefs.copy(virtualKeyCode = value.virtualKeyCode, modifiers = value.modifiers.value)
        }
    }
}
