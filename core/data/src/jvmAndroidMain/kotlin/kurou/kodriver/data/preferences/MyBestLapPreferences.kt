package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.MY_BEST_LAP_VOICE_TYPE_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class MyBestLapPreferences(
    @ProtoNumber(1) val voiceType: String = MY_BEST_LAP_VOICE_TYPE_DEFAULT.id,
    // LMU専用。voiceType は既存データとの互換性のため維持する。
    @ProtoNumber(3) val lmuWindowsReadoutText: String = LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT,
)
