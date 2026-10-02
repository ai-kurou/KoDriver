package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val VoicePreferencesSerializer: Serializer<VoicePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = VoicePreferences(),
        kSerializer = VoicePreferences.serializer(),
    )
