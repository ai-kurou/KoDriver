package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createOverlayBackgroundOpacityPreferencesDataStore(
    directory: String,
): DataStore<OverlayBackgroundOpacityPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "overlay_background_opacity_preferences.pb",
        serializer = OverlayBackgroundOpacityPreferencesSerializer,
    )
