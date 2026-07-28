package kurou.kodriver.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_DEFAULT_THRESHOLD_PERCENTAGE

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class AceWindowsRemainingFuelPreferences(
    @ProtoNumber(1) val thresholdPercentage: Int = ACE_WINDOWS_REMAINING_FUEL_DEFAULT_THRESHOLD_PERCENTAGE,
)
