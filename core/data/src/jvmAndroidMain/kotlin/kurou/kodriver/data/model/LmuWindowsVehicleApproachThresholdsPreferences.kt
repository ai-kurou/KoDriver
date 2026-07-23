package kurou.kodriver.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_DEFAULT_LATERAL_THRESHOLD_METERS
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_DEFAULT_LONGITUDINAL_THRESHOLD_METERS
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class LmuWindowsVehicleApproachThresholdsPreferences(
    @ProtoNumber(1) val longitudinalThresholdMeters: Double =
        LMU_WINDOWS_VEHICLE_APPROACH_DEFAULT_LONGITUDINAL_THRESHOLD_METERS,
    @ProtoNumber(2) val lateralThresholdMeters: Double =
        LMU_WINDOWS_VEHICLE_APPROACH_DEFAULT_LATERAL_THRESHOLD_METERS,
    @ProtoNumber(3) val sustainedApproachDurationSeconds: Int =
        LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT,
)
