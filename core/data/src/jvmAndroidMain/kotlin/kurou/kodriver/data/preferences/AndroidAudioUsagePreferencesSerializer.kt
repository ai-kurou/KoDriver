package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val AndroidAudioUsagePreferencesSerializer: Serializer<AndroidAudioUsagePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = AndroidAudioUsagePreferences(),
        kSerializer = AndroidAudioUsagePreferences.serializer(),
    )
