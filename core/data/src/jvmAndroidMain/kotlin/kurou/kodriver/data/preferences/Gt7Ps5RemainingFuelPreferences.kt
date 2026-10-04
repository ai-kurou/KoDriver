package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class Gt7Ps5RemainingFuelPreferences(
    @ProtoNumber(1) val thresholdPercentage: Int = GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT,
    @ProtoNumber(2) val readoutText: String = GT7_PS5_REMAINING_FUEL_READOUT_TEXT_DEFAULT,
)
