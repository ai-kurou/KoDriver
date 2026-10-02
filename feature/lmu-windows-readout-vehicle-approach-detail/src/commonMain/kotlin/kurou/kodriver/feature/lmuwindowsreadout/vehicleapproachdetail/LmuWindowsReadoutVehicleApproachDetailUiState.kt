package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_START_LEFT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_START_RIGHT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT
import kurou.kodriver.domain.model.VehicleApproachSustainedReadoutType

internal data class LmuWindowsReadoutVehicleApproachDetailUiState(
    val lateralThresholdMeters: Double = LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT,
    val longitudinalThresholdMeters: Double = LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT,
    val sustainedApproachDurationSeconds: Int = LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT,
    val skipFirstLap: Boolean = true,
    val startReadoutEnabled: Boolean = true,
    val startLeftText: String = LMU_WINDOWS_VEHICLE_APPROACH_START_LEFT_READOUT_TEXT_DEFAULT,
    val startRightText: String = LMU_WINDOWS_VEHICLE_APPROACH_START_RIGHT_READOUT_TEXT_DEFAULT,
    val sustainedReadoutEnabled: Boolean = false,
    val sustainedReadoutType: VehicleApproachSustainedReadoutType = VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT,
)
