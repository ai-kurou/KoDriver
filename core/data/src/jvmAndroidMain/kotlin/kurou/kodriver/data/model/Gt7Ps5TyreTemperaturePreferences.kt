package kurou.kodriver.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class Gt7Ps5TyreTemperaturePreferences(
    @ProtoNumber(1) val highThresholdCelsius: Int = GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT,
    @ProtoNumber(2) val enabledStates: Map<String, Boolean> = emptyMap(),
)
