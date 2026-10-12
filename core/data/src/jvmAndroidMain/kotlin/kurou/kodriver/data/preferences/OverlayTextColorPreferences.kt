package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.OVERLAY_TEXT_COLOR_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class OverlayTextColorPreferences(
    @ProtoNumber(1) val argb: Long = OVERLAY_TEXT_COLOR_DEFAULT,
)
