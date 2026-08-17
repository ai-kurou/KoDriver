package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class AceWindowsTyreTemperaturePreferences(
    @ProtoNumber(1) val highThresholdCelsius: Int = ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT,
    @ProtoNumber(2) val enabledStates: Map<String, Boolean> = emptyMap(),
)
