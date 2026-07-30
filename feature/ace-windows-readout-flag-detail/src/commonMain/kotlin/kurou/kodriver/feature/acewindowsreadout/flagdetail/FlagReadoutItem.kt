package kurou.kodriver.feature.acewindowsreadout.flagdetail

import kodriver.feature.acewindowsreadout.flagdetail.generated.resources.Res
import kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_black
import kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_black_white
import kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_blue
import kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_checkered
import kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_green
import kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_orange_circle
import kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_red
import kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_red_yellow_stripes
import kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_white
import kodriver.feature.acewindowsreadout.flagdetail.generated.resources.flag_yellow
import kurou.kodriver.domain.model.ReadoutItemKey
import org.jetbrains.compose.resources.StringResource

internal enum class FlagReadoutItem(
    val key: ReadoutItemKey,
    val labelRes: StringResource,
) {
    WhiteFlag(key = ReadoutItemKey.AceWindows.Flag.WhiteFlag, labelRes = Res.string.flag_white),
    GreenFlag(key = ReadoutItemKey.AceWindows.Flag.GreenFlag, labelRes = Res.string.flag_green),
    RedFlag(key = ReadoutItemKey.AceWindows.Flag.RedFlag, labelRes = Res.string.flag_red),
    BlueFlag(key = ReadoutItemKey.AceWindows.Flag.BlueFlag, labelRes = Res.string.flag_blue),
    YellowFlag(key = ReadoutItemKey.AceWindows.Flag.YellowFlag, labelRes = Res.string.flag_yellow),
    BlackFlag(key = ReadoutItemKey.AceWindows.Flag.BlackFlag, labelRes = Res.string.flag_black),
    BlackWhiteFlag(key = ReadoutItemKey.AceWindows.Flag.BlackWhiteFlag, labelRes = Res.string.flag_black_white),
    CheckeredFlag(key = ReadoutItemKey.AceWindows.Flag.CheckeredFlag, labelRes = Res.string.flag_checkered),
    OrangeCircleFlag(
        key = ReadoutItemKey.AceWindows.Flag.OrangeCircleFlag,
        labelRes = Res.string.flag_orange_circle,
    ),
    RedYellowStripesFlag(
        key = ReadoutItemKey.AceWindows.Flag.RedYellowStripesFlag,
        labelRes = Res.string.flag_red_yellow_stripes,
    ),
}
