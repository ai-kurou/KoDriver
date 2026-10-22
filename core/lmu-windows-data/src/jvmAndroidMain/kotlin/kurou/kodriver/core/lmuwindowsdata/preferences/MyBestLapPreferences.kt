package kurou.kodriver.core.lmuwindowsdata.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class MyBestLapPreferences(
    // 旧口調設定。現在はどのシミュレーターも参照しないが、保存済みデータとの互換性のため維持する。
    @ProtoNumber(1) val voiceType: String = "formal",
    // 番号2（GT7）・4（ACE）は共有 proto の番号。LMU は参照せず、他シミュレーターのドメインに依存しないため定義しない。
    // LMU専用。
    @ProtoNumber(3) val lmuWindowsReadoutText: String = LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT,
)
