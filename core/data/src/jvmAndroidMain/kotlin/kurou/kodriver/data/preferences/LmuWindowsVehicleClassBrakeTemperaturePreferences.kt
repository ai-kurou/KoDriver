package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LmuWindowsVehicleClassBrakeTemperaturePreferences(
    @ProtoNumber(1) val highThresholdCelsiusByVehicleClass: Map<String, Int> = emptyMap(),
    @ProtoNumber(2) val selectedVehicleClassKey: String = "",
    @ProtoNumber(3) val readoutText: String = LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT,
)
