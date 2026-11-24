package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createMicrophoneCutQuietSoundPreferencesDataStore(
    directory: String,
): DataStore<MicrophoneCutQuietSoundPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "microphone_cut_quiet_sound_preferences.pb",
        serializer = MicrophoneCutQuietSoundPreferencesSerializer,
    )
