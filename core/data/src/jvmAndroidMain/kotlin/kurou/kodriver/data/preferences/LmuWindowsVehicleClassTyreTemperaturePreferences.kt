package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LmuWindowsVehicleClassTyreTemperaturePreferences(
    @ProtoNumber(1) val highThresholdCelsiusByVehicleClass: Map<String, Int> = emptyMap(),
    @ProtoNumber(2) val selectedVehicleClassKey: String = "",
)
