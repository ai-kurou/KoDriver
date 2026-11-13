package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createSpeechRecognitionResumeDelayPreferencesDataStore(
    directory: String,
): DataStore<SpeechRecognitionResumeDelayPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "speech_recognition_resume_delay_preferences.pb",
        serializer = SpeechRecognitionResumeDelayPreferencesSerializer,
    )
