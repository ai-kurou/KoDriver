package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val OverlayBackgroundOpacityPreferencesSerializer: Serializer<OverlayBackgroundOpacityPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = OverlayBackgroundOpacityPreferences(),
        kSerializer = OverlayBackgroundOpacityPreferences.serializer(),
    )
