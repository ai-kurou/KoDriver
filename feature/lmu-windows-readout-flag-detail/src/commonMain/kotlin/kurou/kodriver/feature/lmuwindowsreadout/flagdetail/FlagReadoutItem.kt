package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_blue
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_full_course_yellow
import kurou.kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_yellow
import org.jetbrains.compose.resources.StringResource

internal enum class FlagReadoutItem(
    val key: ReadoutItemKey,
    val labelRes: StringResource,
    val chipLabelRes: StringResource,
    val previewEvent: SpeechEvent,
) {
    BlueFlag(
        key = ReadoutItemKey.LmuWindows.Flag.BlueFlag,
        labelRes = Res.string.flag_blue,
        chipLabelRes = Res.string.flag_blue,
        previewEvent = SpeechEvent.BlueFlag,
    ),
    SectorYellowFlag(
        key = ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag,
        labelRes = Res.string.flag_yellow,
        chipLabelRes = Res.string.flag_yellow,
        previewEvent = SpeechEvent.YellowFlag,
    ),
    FullCourseYellow(
        key = ReadoutItemKey.LmuWindows.Flag.FullCourseYellow,
        labelRes = Res.string.flag_full_course_yellow,
        chipLabelRes = Res.string.flag_full_course_yellow,
        previewEvent = SpeechEvent.FullCourseYellow,
    ),
}
