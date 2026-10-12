package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

/** LMU の現在値読み上げホットキーを保存する。 */
internal fun createReadoutHotkeyPreferencesDataStore(directory: String): DataStore<ReadoutHotkeyPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "lmu_windows_readout_hotkey_preferences.pb",
        serializer = ReadoutHotkeyPreferencesSerializer,
    )
