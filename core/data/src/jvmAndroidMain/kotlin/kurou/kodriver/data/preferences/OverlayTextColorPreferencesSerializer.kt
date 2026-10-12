package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val OverlayTextColorPreferencesSerializer: Serializer<OverlayTextColorPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = OverlayTextColorPreferences(),
        kSerializer = OverlayTextColorPreferences.serializer(),
    )
