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
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ReadoutItemKey
import org.jetbrains.compose.resources.StringResource

internal enum class FlagReadoutItem(
    val key: ReadoutItemKey,
    val labelRes: StringResource,
    val previewEvent: SpeechEvent,
) {
    WhiteFlag(
        key = ReadoutItemKey.AceWindows.Flag.WhiteFlag,
        labelRes = Res.string.flag_white,
        previewEvent = SpeechEvent.AceWindowsWhiteFlag,
    ),
    GreenFlag(
        key = ReadoutItemKey.AceWindows.Flag.GreenFlag,
        labelRes = Res.string.flag_green,
        previewEvent = SpeechEvent.AceWindowsGreenFlag,
    ),
    RedFlag(
        key = ReadoutItemKey.AceWindows.Flag.RedFlag,
        labelRes = Res.string.flag_red,
        previewEvent = SpeechEvent.AceWindowsRedFlag,
    ),
    BlueFlag(
        key = ReadoutItemKey.AceWindows.Flag.BlueFlag,
        labelRes = Res.string.flag_blue,
        previewEvent = SpeechEvent.AceWindowsBlueFlag,
    ),
    YellowFlag(
        key = ReadoutItemKey.AceWindows.Flag.YellowFlag,
        labelRes = Res.string.flag_yellow,
        previewEvent = SpeechEvent.AceWindowsYellowFlag,
    ),
    BlackFlag(
        key = ReadoutItemKey.AceWindows.Flag.BlackFlag,
        labelRes = Res.string.flag_black,
        previewEvent = SpeechEvent.AceWindowsBlackFlag,
    ),
    BlackWhiteFlag(
        key = ReadoutItemKey.AceWindows.Flag.BlackWhiteFlag,
        labelRes = Res.string.flag_black_white,
        previewEvent = SpeechEvent.AceWindowsBlackWhiteFlag,
    ),
    CheckeredFlag(
        key = ReadoutItemKey.AceWindows.Flag.CheckeredFlag,
        labelRes = Res.string.flag_checkered,
        previewEvent = SpeechEvent.AceWindowsCheckeredFlag,
    ),
    OrangeCircleFlag(
        key = ReadoutItemKey.AceWindows.Flag.OrangeCircleFlag,
        labelRes = Res.string.flag_orange_circle,
        previewEvent = SpeechEvent.AceWindowsOrangeCircleFlag,
    ),
    RedYellowStripesFlag(
        key = ReadoutItemKey.AceWindows.Flag.RedYellowStripesFlag,
        labelRes = Res.string.flag_red_yellow_stripes,
        previewEvent = SpeechEvent.AceWindowsRedYellowStripesFlag,
    ),
}
