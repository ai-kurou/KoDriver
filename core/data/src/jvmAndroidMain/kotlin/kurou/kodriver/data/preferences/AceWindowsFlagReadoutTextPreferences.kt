package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.ACE_WINDOWS_BLACK_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_BLACK_WHITE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_CHECKERED_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_GREEN_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_ORANGE_CIRCLE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_RED_YELLOW_STRIPES_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_WHITE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT

/** 後続フラッグのProtoNumberは11以降を追加する。既存番号は変更・再利用しない。 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class AceWindowsFlagReadoutTextPreferences(
    @ProtoNumber(1) val checkeredFlagText: String = ACE_WINDOWS_CHECKERED_FLAG_READOUT_TEXT_DEFAULT,
    @ProtoNumber(2) val whiteFlagText: String = ACE_WINDOWS_WHITE_FLAG_READOUT_TEXT_DEFAULT,
    @ProtoNumber(3) val greenFlagText: String = ACE_WINDOWS_GREEN_FLAG_READOUT_TEXT_DEFAULT,
    @ProtoNumber(4) val redFlagText: String = ACE_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT,
    @ProtoNumber(5) val blueFlagText: String = ACE_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT,
    @ProtoNumber(6) val yellowFlagText: String = ACE_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT,
    @ProtoNumber(7) val blackFlagText: String = ACE_WINDOWS_BLACK_FLAG_READOUT_TEXT_DEFAULT,
    @ProtoNumber(8) val blackWhiteFlagText: String = ACE_WINDOWS_BLACK_WHITE_FLAG_READOUT_TEXT_DEFAULT,
    @ProtoNumber(9) val orangeCircleFlagText: String = ACE_WINDOWS_ORANGE_CIRCLE_FLAG_READOUT_TEXT_DEFAULT,
    @ProtoNumber(10) val redYellowStripesFlagText: String = ACE_WINDOWS_RED_YELLOW_STRIPES_FLAG_READOUT_TEXT_DEFAULT,
)
