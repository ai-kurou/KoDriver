package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val ReadoutStartSoundPreferencesSerializer: Serializer<ReadoutStartSoundPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = ReadoutStartSoundPreferences(),
        kSerializer = ReadoutStartSoundPreferences.serializer(),
    )
