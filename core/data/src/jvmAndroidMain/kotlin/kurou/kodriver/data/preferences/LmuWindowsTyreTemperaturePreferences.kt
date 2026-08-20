package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LmuWindowsTyreTemperaturePreferences(
    @ProtoNumber(1) val highThresholdCelsius: Int = LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value,
    @ProtoNumber(2) val enabledStates: Map<String, Boolean> = emptyMap(),
    @ProtoNumber(3) val lowWarningPhases: Map<Int, Boolean> = emptyMap(),
)
