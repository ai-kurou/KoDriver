package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.OVERHEAT_VOICE_TYPE_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class OverheatPreferences(
    @ProtoNumber(1) val voiceType: String = OVERHEAT_VOICE_TYPE_DEFAULT.id,
)
