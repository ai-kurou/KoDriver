package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.MICROPHONE_CUT_QUIET_SOUND_LEVEL_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class MicrophoneCutQuietSoundPreferences(
    @ProtoNumber(1) val level: Int = MICROPHONE_CUT_QUIET_SOUND_LEVEL_DEFAULT,
)
