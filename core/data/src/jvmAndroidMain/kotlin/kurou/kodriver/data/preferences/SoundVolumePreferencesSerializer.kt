package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val SoundVolumePreferencesSerializer: Serializer<SoundVolumePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = SoundVolumePreferences(),
        kSerializer = SoundVolumePreferences.serializer(),
    )
