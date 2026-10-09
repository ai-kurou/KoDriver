package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT

// 旧ProtoBufで省略された閾値を95℃として復元する。新規設定の初期値はSerializerで指定する。
private const val LEGACY_HIGH_THRESHOLD_CELSIUS = 95

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class Gt7Ps5TyreTemperaturePreferences(
    @ProtoNumber(1) val highThresholdCelsius: Int = LEGACY_HIGH_THRESHOLD_CELSIUS,
    @ProtoNumber(2) val enabledStates: Map<String, Boolean> = emptyMap(),
    @ProtoNumber(3) val overheatReadoutText: String = GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT,
)
