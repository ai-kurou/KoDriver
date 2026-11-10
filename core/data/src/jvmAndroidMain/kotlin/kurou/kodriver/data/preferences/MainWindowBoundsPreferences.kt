package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.MAIN_WINDOW_HEIGHT_DEFAULT
import kurou.kodriver.domain.model.MAIN_WINDOW_POSITION_UNSPECIFIED
import kurou.kodriver.domain.model.MAIN_WINDOW_WIDTH_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class MainWindowBoundsPreferences(
    @ProtoNumber(1) val x: Int = MAIN_WINDOW_POSITION_UNSPECIFIED,
    @ProtoNumber(2) val y: Int = MAIN_WINDOW_POSITION_UNSPECIFIED,
    @ProtoNumber(3) val width: Int = MAIN_WINDOW_WIDTH_DEFAULT,
    @ProtoNumber(4) val height: Int = MAIN_WINDOW_HEIGHT_DEFAULT,
)
