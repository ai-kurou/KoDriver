package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.LmuWindowsFlagReadoutTarget
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_blue
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_full_course_yellow
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_red
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_yellow
import org.jetbrains.compose.resources.StringResource

internal enum class FlagReadoutItem(
    val key: ReadoutItemKey,
    val labelRes: StringResource,
    val chipLabelRes: StringResource,
    val previewEvent: SpeechEvent,
    val target: LmuWindowsFlagReadoutTarget,
) {
    BlueFlag(
        key = ReadoutItemKey.LmuWindows.Flag.BlueFlag,
        labelRes = Res.string.flag_blue,
        chipLabelRes = Res.string.flag_blue,
        previewEvent = SpeechEvent.BlueFlag,
        target = LmuWindowsFlagReadoutTarget.BLUE_FLAG,
    ),
    SectorYellowFlag(
        key = ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag,
        labelRes = Res.string.flag_yellow,
        chipLabelRes = Res.string.flag_yellow,
        previewEvent = SpeechEvent.YellowFlag,
        target = LmuWindowsFlagReadoutTarget.SECTOR_YELLOW_FLAG,
    ),
    FullCourseYellow(
        key = ReadoutItemKey.LmuWindows.Flag.FullCourseYellow,
        labelRes = Res.string.flag_full_course_yellow,
        chipLabelRes = Res.string.flag_full_course_yellow,
        previewEvent = SpeechEvent.FullCourseYellow,
        target = LmuWindowsFlagReadoutTarget.FULL_COURSE_YELLOW,
    ),
    RedFlag(
        key = ReadoutItemKey.LmuWindows.Flag.RedFlag,
        labelRes = Res.string.flag_red,
        chipLabelRes = Res.string.flag_red,
        previewEvent = SpeechEvent.RedFlag,
        target = LmuWindowsFlagReadoutTarget.RED_FLAG,
    ),
}
