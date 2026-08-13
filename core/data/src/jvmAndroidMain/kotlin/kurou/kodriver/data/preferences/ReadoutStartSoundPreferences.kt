package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.READOUT_START_SOUND_TYPE_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class ReadoutStartSoundPreferences(
    @ProtoNumber(1) val type: String = READOUT_START_SOUND_TYPE_DEFAULT.id,
)
