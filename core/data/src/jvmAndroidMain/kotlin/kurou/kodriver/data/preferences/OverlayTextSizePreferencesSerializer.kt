package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val OverlayTextSizePreferencesSerializer: Serializer<OverlayTextSizePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = OverlayTextSizePreferences(),
        kSerializer = OverlayTextSizePreferences.serializer(),
    )
