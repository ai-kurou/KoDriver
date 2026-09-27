package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createOverlayWindowBoundsPreferencesDataStore(
    directory: String,
): DataStore<OverlayWindowBoundsPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "overlay_window_bounds_preferences.pb",
        serializer = OverlayWindowBoundsPreferencesSerializer,
    )
