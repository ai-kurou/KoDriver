package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.VOICE_SPEED_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class VoiceSpeedPreferences(
    @ProtoNumber(1) val voiceSpeed: Float = VOICE_SPEED_DEFAULT,
)
