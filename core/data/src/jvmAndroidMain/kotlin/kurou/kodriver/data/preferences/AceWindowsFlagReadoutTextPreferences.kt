package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.ACE_WINDOWS_CHECKERED_FLAG_READOUT_TEXT_DEFAULT

/** 後続フラッグのProtoNumberは2以降を追加する。既存番号は変更・再利用しない。 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class AceWindowsFlagReadoutTextPreferences(
    @ProtoNumber(1) val checkeredFlagText: String = ACE_WINDOWS_CHECKERED_FLAG_READOUT_TEXT_DEFAULT,
)
