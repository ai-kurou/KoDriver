package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.OVERLAY_BACKGROUND_OPACITY_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class OverlayBackgroundOpacityPreferences(
    @ProtoNumber(1) val opacity: Int = OVERLAY_BACKGROUND_OPACITY_DEFAULT,
)
