package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.MANUAL_READOUT_PLAY_BUTTON_LAYOUT_ENABLED_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class ManualReadoutPlayButtonLayoutPreferences(
    @ProtoNumber(1) val enabled: Boolean = MANUAL_READOUT_PLAY_BUTTON_LAYOUT_ENABLED_DEFAULT,
)
