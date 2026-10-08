package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.ACE_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class MyBestLapPreferences(
    // 旧口調設定。現在はどのシミュレーターも参照しないが、保存済みデータとの互換性のため維持する。
    @ProtoNumber(1) val voiceType: String = "formal",
    // GT7 専用の文言。
    @ProtoNumber(2) val readoutText: String = GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT,
    // LMU専用。
    @ProtoNumber(3) val lmuWindowsReadoutText: String = LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT,
    // ACE専用。
    @ProtoNumber(4) val aceWindowsReadoutText: String = ACE_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT,
)
