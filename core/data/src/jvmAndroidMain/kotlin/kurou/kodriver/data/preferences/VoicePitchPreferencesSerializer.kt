package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val VoicePitchPreferencesSerializer: Serializer<VoicePitchPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = VoicePitchPreferences(),
        kSerializer = VoicePitchPreferences.serializer(),
    )
