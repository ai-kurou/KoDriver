package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.Res
import kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_blue
import kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_full_course_yellow
import kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_red
import kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_session_stop
import kodriver.feature.lmuwindowsreadout.flagdetail.generated.resources.flag_yellow
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ReadoutItemKey
import org.jetbrains.compose.resources.StringResource

internal enum class FlagReadoutItem(
    val key: ReadoutItemKey,
    val labelRes: StringResource,
    val chipLabelRes: StringResource,
    val previewEvent: SpeechEvent,
) {
    BlueFlag(
        key = ReadoutItemKey.BLUE_FLAG,
        labelRes = Res.string.flag_blue,
        chipLabelRes = Res.string.flag_blue,
        previewEvent = SpeechEvent.BlueFlag,
    ),
    SectorYellowFlag(
        key = ReadoutItemKey.SECTOR_YELLOW_FLAG,
        labelRes = Res.string.flag_yellow,
        chipLabelRes = Res.string.flag_yellow,
        previewEvent = SpeechEvent.YellowFlag,
    ),
    FullCourseYellow(
        key = ReadoutItemKey.FULL_COURSE_YELLOW,
        labelRes = Res.string.flag_full_course_yellow,
        chipLabelRes = Res.string.flag_full_course_yellow,
        previewEvent = SpeechEvent.FullCourseYellow,
    ),
    RedFlag(
        key = ReadoutItemKey.RED_FLAG,
        labelRes = Res.string.flag_red,
        chipLabelRes = Res.string.flag_session_stop,
        previewEvent = SpeechEvent.SessionStop,
    ),
}
