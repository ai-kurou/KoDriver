package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.ANDROID_AUDIO_USAGE_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class AndroidAudioUsagePreferences(
    @ProtoNumber(1) val usage: String = ANDROID_AUDIO_USAGE_DEFAULT.id,
)
