package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val OverlayWindowBoundsPreferencesSerializer: Serializer<OverlayWindowBoundsPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = OverlayWindowBoundsPreferences(),
        kSerializer = OverlayWindowBoundsPreferences.serializer(),
    )
