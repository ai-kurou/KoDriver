package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_START_READOUT_TYPE_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class AceWindowsVehicleApproachPreferences(
    @ProtoNumber(1) val longitudinalThresholdMeters: Double =
        ACE_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT,
    @ProtoNumber(2) val lateralThresholdMeters: Double =
        ACE_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT,
    @ProtoNumber(3) val startReadoutType: String = ACE_WINDOWS_VEHICLE_APPROACH_START_READOUT_TYPE_DEFAULT.id,
    @ProtoNumber(4) val enabledStates: Map<String, Boolean> = emptyMap(),
)
