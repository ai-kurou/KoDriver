package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val OverlayVisiblePreferencesSerializer: Serializer<OverlayVisiblePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = OverlayVisiblePreferences(),
        kSerializer = OverlayVisiblePreferences.serializer(),
    )
