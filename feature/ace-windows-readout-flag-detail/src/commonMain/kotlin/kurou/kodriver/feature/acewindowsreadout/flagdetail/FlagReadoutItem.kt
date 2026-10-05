package kurou.kodriver.feature.acewindowsreadout.flagdetail

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
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.feature.acewindowsreadout.flagdetail.generated.resources.Res
import kurou.kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_black
import kurou.kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_black_white
import kurou.kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_blue
import kurou.kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_checkered
import kurou.kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_green
import kurou.kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_orange_circle
import kurou.kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_red
import kurou.kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_red_yellow_stripes
import kurou.kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_white
import kurou.kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_yellow
import org.jetbrains.compose.resources.StringResource

internal enum class FlagReadoutItem(
    val key: ReadoutItemKey,
    val labelRes: StringResource,
    val defaultText: String,
) {
    WhiteFlag(
        key = ReadoutItemKey.AceWindows.Flag.WhiteFlag,
        labelRes = Res.string.flag_white,
        defaultText = ACE_WINDOWS_WHITE_FLAG_READOUT_TEXT_DEFAULT,
    ),
    GreenFlag(
        key = ReadoutItemKey.AceWindows.Flag.GreenFlag,
        labelRes = Res.string.flag_green,
        defaultText = ACE_WINDOWS_GREEN_FLAG_READOUT_TEXT_DEFAULT,
    ),
    RedFlag(
        key = ReadoutItemKey.AceWindows.Flag.RedFlag,
        labelRes = Res.string.flag_red,
        defaultText = ACE_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT,
    ),
    BlueFlag(
        key = ReadoutItemKey.AceWindows.Flag.BlueFlag,
        labelRes = Res.string.flag_blue,
        defaultText = ACE_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT,
    ),
    YellowFlag(
        key = ReadoutItemKey.AceWindows.Flag.YellowFlag,
        labelRes = Res.string.flag_yellow,
        defaultText = ACE_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT,
    ),
    BlackFlag(
        key = ReadoutItemKey.AceWindows.Flag.BlackFlag,
        labelRes = Res.string.flag_black,
        defaultText = ACE_WINDOWS_BLACK_FLAG_READOUT_TEXT_DEFAULT,
    ),
    BlackWhiteFlag(
        key = ReadoutItemKey.AceWindows.Flag.BlackWhiteFlag,
        labelRes = Res.string.flag_black_white,
        defaultText = ACE_WINDOWS_BLACK_WHITE_FLAG_READOUT_TEXT_DEFAULT,
    ),
    CheckeredFlag(
        key = ReadoutItemKey.AceWindows.Flag.CheckeredFlag,
        labelRes = Res.string.flag_checkered,
        defaultText = ACE_WINDOWS_CHECKERED_FLAG_READOUT_TEXT_DEFAULT,
    ),
    OrangeCircleFlag(
        key = ReadoutItemKey.AceWindows.Flag.OrangeCircleFlag,
        labelRes = Res.string.flag_orange_circle,
        defaultText = ACE_WINDOWS_ORANGE_CIRCLE_FLAG_READOUT_TEXT_DEFAULT,
    ),
    RedYellowStripesFlag(
        key = ReadoutItemKey.AceWindows.Flag.RedYellowStripesFlag,
        labelRes = Res.string.flag_red_yellow_stripes,
        defaultText = ACE_WINDOWS_RED_YELLOW_STRIPES_FLAG_READOUT_TEXT_DEFAULT,
    ),
}
