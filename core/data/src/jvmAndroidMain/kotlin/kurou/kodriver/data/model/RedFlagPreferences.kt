package kurou.kodriver.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.RED_FLAG_VOICE_TYPE_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class RedFlagPreferences(
    @ProtoNumber(1) val voiceType: String = RED_FLAG_VOICE_TYPE_DEFAULT.id,
)
