package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.OVERLAY_WINDOW_HEIGHT_DEFAULT
import kurou.kodriver.domain.model.OVERLAY_WINDOW_POSITION_UNSPECIFIED
import kurou.kodriver.domain.model.OVERLAY_WINDOW_WIDTH_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class OverlayWindowBoundsPreferences(
    @ProtoNumber(1) val x: Int = OVERLAY_WINDOW_POSITION_UNSPECIFIED,
    @ProtoNumber(2) val y: Int = OVERLAY_WINDOW_POSITION_UNSPECIFIED,
    @ProtoNumber(3) val width: Int = OVERLAY_WINDOW_WIDTH_DEFAULT,
    @ProtoNumber(4) val height: Int = OVERLAY_WINDOW_HEIGHT_DEFAULT,
)
