package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.SELECTED_AUDIO_INPUT_DEVICE_NAME_UNSPECIFIED

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class SelectedAudioInputDevicePreferences(
    @ProtoNumber(1) val deviceName: String = SELECTED_AUDIO_INPUT_DEVICE_NAME_UNSPECIFIED,
)
