package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore

internal fun createSelectedAudioInputDevicePreferencesDataStore(
    directory: String,
): DataStore<SelectedAudioInputDevicePreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "selected_audio_input_device_preferences.pb",
        serializer = SelectedAudioInputDevicePreferencesSerializer,
    )
