package kurou.kodriver.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class VehicleApproachPreferences(
    @ProtoNumber(1) val skipFirstLap: Boolean = true,
    @ProtoNumber(2) val startReadoutEnabled: Boolean = true,
    @ProtoNumber(3) val startReadoutType: String = "car_left_right",
)
