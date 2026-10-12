package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createOverlayTextColorPreferencesDataStore(directory: String): DataStore<OverlayTextColorPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "overlay_text_color_preferences.pb",
        serializer = OverlayTextColorPreferencesSerializer,
    )
