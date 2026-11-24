package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val MicrophoneCutQuietSoundPreferencesSerializer: Serializer<MicrophoneCutQuietSoundPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = MicrophoneCutQuietSoundPreferences(),
        kSerializer = MicrophoneCutQuietSoundPreferences.serializer(),
    )
