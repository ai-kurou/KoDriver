package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val ReadoutStartSoundEnabledPreferencesSerializer: Serializer<ReadoutStartSoundEnabledPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = ReadoutStartSoundEnabledPreferences(),
        kSerializer = ReadoutStartSoundEnabledPreferences.serializer(),
    )
