package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class AceWindowsVehicleApproachPreferences(
    @ProtoNumber(1) val thresholdMeters: Double = ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT,
    @ProtoNumber(4) val enabledStates: Map<String, Boolean> = emptyMap(),
)
