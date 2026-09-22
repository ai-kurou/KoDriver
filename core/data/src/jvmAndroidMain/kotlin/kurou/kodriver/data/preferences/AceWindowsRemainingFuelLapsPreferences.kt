package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class AceWindowsRemainingFuelLapsPreferences(
    @ProtoNumber(1) val thresholdLaps: Int = ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT,
)
