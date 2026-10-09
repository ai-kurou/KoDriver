package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createVoiceSpeedPreferencesDataStore(directory: String): DataStore<VoiceSpeedPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "voice_speed_preferences.pb",
        serializer = VoiceSpeedPreferencesSerializer,
    )
