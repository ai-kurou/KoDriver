package kurou.kodriver.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.MY_BEST_LAP_VOICE_TYPE_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class MyBestLapPreferences(
    @ProtoNumber(1) val voiceType: String = MY_BEST_LAP_VOICE_TYPE_DEFAULT.id,
)
