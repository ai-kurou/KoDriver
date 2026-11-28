package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createWakeWordHotkeyPreferencesDataStore(directory: String): DataStore<WakeWordHotkeyPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "wake_word_hotkey_preferences.pb",
        serializer = WakeWordHotkeyPreferencesSerializer,
    )
