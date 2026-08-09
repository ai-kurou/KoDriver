package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import kurou.kodriver.core.model.VehicleApproachStartReadoutType
import kurou.kodriver.core.model.VehicleApproachSustainedReadoutType
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT

internal data class LmuWindowsReadoutVehicleApproachDetailUiState(
    val lateralThresholdMeters: Double = LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT,
    val longitudinalThresholdMeters: Double = LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT,
    val sustainedApproachDurationSeconds: Int = LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT,
    val skipFirstLap: Boolean = true,
    val startReadoutEnabled: Boolean = true,
    val startReadoutType: VehicleApproachStartReadoutType = VehicleApproachStartReadoutType.CAR_LEFT_RIGHT,
    val sustainedReadoutEnabled: Boolean = false,
    val sustainedReadoutType: VehicleApproachSustainedReadoutType = VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT,
)
