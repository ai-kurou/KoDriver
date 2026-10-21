package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createAndroidAudioUsagePreferencesDataStore(directory: String): DataStore<AndroidAudioUsagePreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "android_audio_usage_preferences.pb",
        serializer = AndroidAudioUsagePreferencesSerializer,
    )
