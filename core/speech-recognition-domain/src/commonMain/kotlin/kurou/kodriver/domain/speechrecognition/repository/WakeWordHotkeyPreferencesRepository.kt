package kurou.kodriver.domain.speechrecognition.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.speechrecognition.model.WakeWordHotkey

public interface WakeWordHotkeyPreferencesRepository {
    public fun observeWakeWordHotkey(): Flow<WakeWordHotkey>

    public suspend fun saveWakeWordHotkey(hotkey: WakeWordHotkey)
}
