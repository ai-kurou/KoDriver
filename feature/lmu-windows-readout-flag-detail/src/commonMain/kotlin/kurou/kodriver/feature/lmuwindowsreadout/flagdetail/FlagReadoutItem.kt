package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import kurou.kodriver.domain.model.LMU_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_FULL_COURSE_YELLOW_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT
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
    /** カスタム読み上げ文言の初期値。 */
    val defaultText: String,
) {
    BlueFlag(
        key = ReadoutItemKey.LmuWindows.Flag.BlueFlag,
        labelRes = Res.string.flag_blue,
        defaultText = LMU_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT,
    ),
    SectorYellowFlag(
        key = ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag,
        labelRes = Res.string.flag_yellow,
        defaultText = LMU_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT,
    ),
    FullCourseYellow(
        key = ReadoutItemKey.LmuWindows.Flag.FullCourseYellow,
        labelRes = Res.string.flag_full_course_yellow,
        defaultText = LMU_WINDOWS_FULL_COURSE_YELLOW_FLAG_READOUT_TEXT_DEFAULT,
    ),
    RedFlag(
        key = ReadoutItemKey.LmuWindows.Flag.RedFlag,
        labelRes = Res.string.flag_red,
        defaultText = LMU_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT,
    ),
}
