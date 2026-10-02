package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createVoicePreferencesDataStore(directory: String): DataStore<VoicePreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "voice_preferences.pb",
        serializer = VoicePreferencesSerializer,
    )
