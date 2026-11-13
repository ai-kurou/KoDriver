package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val SpeechRecognitionResumeDelayPreferencesSerializer: Serializer<SpeechRecognitionResumeDelayPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = SpeechRecognitionResumeDelayPreferences(),
        kSerializer = SpeechRecognitionResumeDelayPreferences.serializer(),
    )
