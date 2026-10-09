package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val VoiceSpeedPreferencesSerializer: Serializer<VoiceSpeedPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = VoiceSpeedPreferences(),
        kSerializer = VoiceSpeedPreferences.serializer(),
    )
