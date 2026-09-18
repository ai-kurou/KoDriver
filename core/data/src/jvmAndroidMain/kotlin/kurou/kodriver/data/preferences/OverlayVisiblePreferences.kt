package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.OVERLAY_VISIBLE_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class OverlayVisiblePreferences(
    @ProtoNumber(1) val visible: Boolean = OVERLAY_VISIBLE_DEFAULT,
)
