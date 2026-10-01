package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.LMU_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_FULL_COURSE_YELLOW_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT

/** 旧収録音声選択のProtoNumber 5〜8は廃止済み。既存データとの互換性のため再利用しない。 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class FlagReadoutTextPreferences(
    @ProtoNumber(1) val sectorYellowFlagText: String = LMU_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT,
    @ProtoNumber(2) val blueFlagText: String = LMU_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT,
    @ProtoNumber(3) val fullCourseYellowFlagText: String = LMU_WINDOWS_FULL_COURSE_YELLOW_FLAG_READOUT_TEXT_DEFAULT,
    @ProtoNumber(4) val redFlagText: String = LMU_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT,
)
