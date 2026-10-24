package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val LowVolumeWarningEnabledPreferencesSerializer: Serializer<LowVolumeWarningEnabledPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = LowVolumeWarningEnabledPreferences(),
        kSerializer = LowVolumeWarningEnabledPreferences.serializer(),
    )
