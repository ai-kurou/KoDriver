package kurou.kodriver.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_DEFAULT_HIGH_THRESHOLD_CELSIUS

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LmuWindowsTyreTemperaturePreferences(
    @ProtoNumber(1) val highThresholdCelsius: Int = LMU_WINDOWS_TYRE_TEMPERATURE_DEFAULT_HIGH_THRESHOLD_CELSIUS,
    @ProtoNumber(2) val enabledStates: Map<String, Boolean> = emptyMap(),
    @ProtoNumber(3) val lowWarningPhases: Map<Int, Boolean> = emptyMap(),
)
