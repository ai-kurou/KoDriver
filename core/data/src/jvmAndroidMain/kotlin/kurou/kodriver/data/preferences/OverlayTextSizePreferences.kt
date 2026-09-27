package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.OVERLAY_TEXT_SIZE_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class OverlayTextSizePreferences(
    @ProtoNumber(1) val size: String = OVERLAY_TEXT_SIZE_DEFAULT.id,
)
