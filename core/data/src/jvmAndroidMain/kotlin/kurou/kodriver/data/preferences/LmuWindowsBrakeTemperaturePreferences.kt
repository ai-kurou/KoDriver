package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LmuWindowsBrakeTemperaturePreferences(
    @ProtoNumber(1) val highThresholdCelsius: Int = LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT,
)
