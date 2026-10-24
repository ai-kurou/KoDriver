package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.LOW_VOLUME_WARNING_ENABLED_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LowVolumeWarningEnabledPreferences(
    @ProtoNumber(1) val enabled: Boolean = LOW_VOLUME_WARNING_ENABLED_DEFAULT,
)
