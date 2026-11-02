package kurou.kodriver.feature.acewindowsautoreadout.flagdetail

import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_BLACK_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_BLACK_WHITE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_CHECKERED_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_GREEN_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_ORANGE_CIRCLE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_RED_YELLOW_STRIPES_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_WHITE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagReadoutTextKey
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.feature.acewindowsautoreadout.flagdetail.generated.resources.Res
import kurou.kodriver.feature.acewindowsautoreadout.flagdetail.generated.resources.flag_black
import kurou.kodriver.feature.acewindowsautoreadout.flagdetail.generated.resources.flag_black_white
import kurou.kodriver.feature.acewindowsautoreadout.flagdetail.generated.resources.flag_blue
import kurou.kodriver.feature.acewindowsautoreadout.flagdetail.generated.resources.flag_checkered
import kurou.kodriver.feature.acewindowsautoreadout.flagdetail.generated.resources.flag_green
import kurou.kodriver.feature.acewindowsautoreadout.flagdetail.generated.resources.flag_orange_circle
import kurou.kodriver.feature.acewindowsautoreadout.flagdetail.generated.resources.flag_red
import kurou.kodriver.feature.acewindowsautoreadout.flagdetail.generated.resources.flag_red_yellow_stripes
import kurou.kodriver.feature.acewindowsautoreadout.flagdetail.generated.resources.flag_white
import kurou.kodriver.feature.acewindowsautoreadout.flagdetail.generated.resources.flag_yellow
import org.jetbrains.compose.resources.StringResource

internal enum class FlagReadoutItem(
    val textKey: AceWindowsFlagReadoutTextKey,
    val key: ReadoutItemKey,
    val labelRes: StringResource,
    val defaultText: String,
) {
    WhiteFlag(
        textKey = AceWindowsFlagReadoutTextKey.WHITE,
        key = AceWindowsReadoutItemKey.Flag.WhiteFlag,
        labelRes = Res.string.flag_white,
        defaultText = ACE_WINDOWS_WHITE_FLAG_READOUT_TEXT_DEFAULT,
    ),
    GreenFlag(
        textKey = AceWindowsFlagReadoutTextKey.GREEN,
        key = AceWindowsReadoutItemKey.Flag.GreenFlag,
        labelRes = Res.string.flag_green,
        defaultText = ACE_WINDOWS_GREEN_FLAG_READOUT_TEXT_DEFAULT,
    ),
    RedFlag(
        textKey = AceWindowsFlagReadoutTextKey.RED,
        key = AceWindowsReadoutItemKey.Flag.RedFlag,
        labelRes = Res.string.flag_red,
        defaultText = ACE_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT,
    ),
    BlueFlag(
        textKey = AceWindowsFlagReadoutTextKey.BLUE,
        key = AceWindowsReadoutItemKey.Flag.BlueFlag,
        labelRes = Res.string.flag_blue,
        defaultText = ACE_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT,
    ),
    YellowFlag(
        textKey = AceWindowsFlagReadoutTextKey.YELLOW,
        key = AceWindowsReadoutItemKey.Flag.YellowFlag,
        labelRes = Res.string.flag_yellow,
        defaultText = ACE_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT,
    ),
    BlackFlag(
        textKey = AceWindowsFlagReadoutTextKey.BLACK,
        key = AceWindowsReadoutItemKey.Flag.BlackFlag,
        labelRes = Res.string.flag_black,
        defaultText = ACE_WINDOWS_BLACK_FLAG_READOUT_TEXT_DEFAULT,
    ),
    BlackWhiteFlag(
        textKey = AceWindowsFlagReadoutTextKey.BLACK_WHITE,
        key = AceWindowsReadoutItemKey.Flag.BlackWhiteFlag,
        labelRes = Res.string.flag_black_white,
        defaultText = ACE_WINDOWS_BLACK_WHITE_FLAG_READOUT_TEXT_DEFAULT,
    ),
    CheckeredFlag(
        textKey = AceWindowsFlagReadoutTextKey.CHECKERED,
        key = AceWindowsReadoutItemKey.Flag.CheckeredFlag,
        labelRes = Res.string.flag_checkered,
        defaultText = ACE_WINDOWS_CHECKERED_FLAG_READOUT_TEXT_DEFAULT,
    ),
    OrangeCircleFlag(
        textKey = AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE,
        key = AceWindowsReadoutItemKey.Flag.OrangeCircleFlag,
        labelRes = Res.string.flag_orange_circle,
        defaultText = ACE_WINDOWS_ORANGE_CIRCLE_FLAG_READOUT_TEXT_DEFAULT,
    ),
    RedYellowStripesFlag(
        textKey = AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES,
        key = AceWindowsReadoutItemKey.Flag.RedYellowStripesFlag,
        labelRes = Res.string.flag_red_yellow_stripes,
        defaultText = ACE_WINDOWS_RED_YELLOW_STRIPES_FLAG_READOUT_TEXT_DEFAULT,
    ),
}
