package kurou.kodriver.core.acewindowsdata.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class MyBestLapPreferences(
    // 旧口調設定。現在は参照しないが、保存済みデータとの互換性のため維持する。
    @ProtoNumber(1) val voiceType: String = "formal",
    // 番号2（GT7）・3（LMU）は他シミュレーター固有のため定義しない。ACE専用。
    @ProtoNumber(4) val aceWindowsReadoutText: String = ACE_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT,
)
