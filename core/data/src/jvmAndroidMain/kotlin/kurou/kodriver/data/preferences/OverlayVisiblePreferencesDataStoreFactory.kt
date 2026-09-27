package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createOverlayVisiblePreferencesDataStore(directory: String): DataStore<OverlayVisiblePreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "overlay_visible_preferences.pb",
        serializer = OverlayVisiblePreferencesSerializer,
    )
