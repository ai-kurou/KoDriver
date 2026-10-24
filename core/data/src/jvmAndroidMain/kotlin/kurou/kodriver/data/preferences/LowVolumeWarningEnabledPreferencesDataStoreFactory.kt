package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createLowVolumeWarningEnabledPreferencesDataStore(
    directory: String,
): DataStore<LowVolumeWarningEnabledPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "low_volume_warning_enabled_preferences.pb",
        serializer = LowVolumeWarningEnabledPreferencesSerializer,
    )
