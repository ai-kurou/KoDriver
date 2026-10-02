package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class VoicePreferences(
    @ProtoNumber(1) val voiceId: String = VOICE_ID_UNSPECIFIED,
)
