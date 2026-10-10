package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createVoicePitchPreferencesDataStore(directory: String): DataStore<VoicePitchPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "voice_pitch_preferences.pb",
        serializer = VoicePitchPreferencesSerializer,
    )
