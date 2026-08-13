package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class Gt7Ps5RemainingFuelLapsPreferences(
    @ProtoNumber(1) val remainingFuelLaps: Int = GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT,
)
