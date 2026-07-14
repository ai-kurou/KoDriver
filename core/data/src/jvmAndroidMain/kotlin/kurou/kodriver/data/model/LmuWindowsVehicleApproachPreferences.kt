package kurou.kodriver.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LmuWindowsVehicleApproachPreferences(
    @ProtoNumber(1) val skipFirstLap: Boolean = true,
    @ProtoNumber(3) val startReadoutType: String = "car_left_right",
    @ProtoNumber(4) val enabledStates: Map<String, Boolean> = emptyMap(),
)
