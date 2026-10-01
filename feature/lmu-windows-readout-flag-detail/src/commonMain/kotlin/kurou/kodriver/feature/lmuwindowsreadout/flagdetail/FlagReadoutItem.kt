package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.LMU_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_FULL_COURSE_YELLOW_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LmuWindowsFlagReadoutTarget
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_DEFAULT
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
    /** 収録音声のチップを表示し、収録音声とカスタム文言を切り替えられるか。false の項目は自由文字列の読み上げのみ。 */
    val recordedVoiceSelectable: Boolean = true,
    /** カスタム読み上げ文言の初期値。 */
    val defaultText: String = READOUT_CUSTOM_TEXT_DEFAULT,
) {
    BlueFlag(
        key = ReadoutItemKey.LmuWindows.Flag.BlueFlag,
        labelRes = Res.string.flag_blue,
        chipLabelRes = Res.string.flag_blue,
        previewEvent = SpeechEvent.BlueFlag,
        target = LmuWindowsFlagReadoutTarget.BLUE_FLAG,
        recordedVoiceSelectable = false,
        defaultText = LMU_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT,
    ),
    SectorYellowFlag(
        key = ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag,
        labelRes = Res.string.flag_yellow,
        chipLabelRes = Res.string.flag_yellow,
        previewEvent = SpeechEvent.YellowFlag,
        target = LmuWindowsFlagReadoutTarget.SECTOR_YELLOW_FLAG,
        recordedVoiceSelectable = false,
        defaultText = LMU_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT,
    ),
    FullCourseYellow(
        key = ReadoutItemKey.LmuWindows.Flag.FullCourseYellow,
        labelRes = Res.string.flag_full_course_yellow,
        chipLabelRes = Res.string.flag_full_course_yellow,
        previewEvent = SpeechEvent.FullCourseYellow,
        target = LmuWindowsFlagReadoutTarget.FULL_COURSE_YELLOW,
        recordedVoiceSelectable = false,
        defaultText = LMU_WINDOWS_FULL_COURSE_YELLOW_FLAG_READOUT_TEXT_DEFAULT,
    ),
    RedFlag(
        key = ReadoutItemKey.LmuWindows.Flag.RedFlag,
        labelRes = Res.string.flag_red,
        chipLabelRes = Res.string.flag_red,
        previewEvent = SpeechEvent.RedFlag,
        target = LmuWindowsFlagReadoutTarget.RED_FLAG,
    ),
}
