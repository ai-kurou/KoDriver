package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.VOICE_PITCH_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class VoicePitchPreferences(
    @ProtoNumber(1) val voicePitch: Float = VOICE_PITCH_DEFAULT,
)
