package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.LMU_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_DEFAULT
import kurou.kodriver.domain.model.READOUT_RECORDED_VOICE_SELECTED_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class FlagReadoutTextPreferences(
    @ProtoNumber(1) val sectorYellowFlagText: String = LMU_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT,
    @ProtoNumber(2) val blueFlagText: String = LMU_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT,
    @ProtoNumber(3) val fullCourseYellowFlagText: String = READOUT_CUSTOM_TEXT_DEFAULT,
    @ProtoNumber(4) val redFlagText: String = READOUT_CUSTOM_TEXT_DEFAULT,
    @ProtoNumber(5) val sectorYellowFlagRecordedVoiceSelected: Boolean = READOUT_RECORDED_VOICE_SELECTED_DEFAULT,
    @ProtoNumber(6) val blueFlagRecordedVoiceSelected: Boolean = READOUT_RECORDED_VOICE_SELECTED_DEFAULT,
    @ProtoNumber(7) val fullCourseYellowFlagRecordedVoiceSelected: Boolean = READOUT_RECORDED_VOICE_SELECTED_DEFAULT,
    @ProtoNumber(8) val redFlagRecordedVoiceSelected: Boolean = READOUT_RECORDED_VOICE_SELECTED_DEFAULT,
)
