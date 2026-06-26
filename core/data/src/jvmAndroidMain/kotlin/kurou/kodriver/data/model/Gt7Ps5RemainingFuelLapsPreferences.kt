package kurou.kodriver.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class Gt7Ps5RemainingFuelLapsPreferences(
    @ProtoNumber(1) val remainingFuelLaps: Int = 3,
)
