package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createOverlayTextSizePreferencesDataStore(directory: String): DataStore<OverlayTextSizePreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "overlay_text_size_preferences.pb",
        serializer = OverlayTextSizePreferencesSerializer,
    )
