package kurou.kodriver.core.gt7ps5data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class MyBestLapPreferences(
    // 旧口調設定。現在は参照しないが、保存済みデータとの互換性のため維持する。
    @ProtoNumber(1) val voiceType: String = "formal",
    // 番号3（LMU）・4（ACE）は他シミュレーター固有のため定義しない。GT7専用。
    @ProtoNumber(2) val readoutText: String = GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT,
)
