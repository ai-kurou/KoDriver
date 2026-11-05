package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val SelectedAudioInputDevicePreferencesSerializer: Serializer<SelectedAudioInputDevicePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = SelectedAudioInputDevicePreferences(),
        kSerializer = SelectedAudioInputDevicePreferences.serializer(),
    )
