package kurou.kodriver.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LmuWindowsTyreTemperaturePreferences(
    @ProtoNumber(1) val highThresholdCelsius: Int = 90,
)
