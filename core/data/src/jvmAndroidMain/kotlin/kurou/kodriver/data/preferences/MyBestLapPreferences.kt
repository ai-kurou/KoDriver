package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.ACE_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.MY_BEST_LAP_VOICE_TYPE_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class MyBestLapPreferences(
    @ProtoNumber(1) val voiceType: String = MY_BEST_LAP_VOICE_TYPE_DEFAULT.id,
    // GT7 専用の文言。voiceType は ACE と旧データの互換性のため維持する。
    @ProtoNumber(2) val readoutText: String = GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT,
    // LMU専用。voiceType は既存データとの互換性のため維持する。
    @ProtoNumber(3) val lmuWindowsReadoutText: String = LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT,
    // ACE専用。voiceType は旧データとの互換性のため維持する。
    @ProtoNumber(4) val aceWindowsReadoutText: String = ACE_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT,
)
