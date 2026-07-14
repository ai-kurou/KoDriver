package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.model.VehicleApproachSustainedReadoutType

internal data class LmuWindowsReadoutVehicleApproachDetailUiState(
    val lateralThresholdMeters: Double = 5.0,
    val longitudinalThresholdMeters: Double = 5.0,
    val sustainedApproachDurationSeconds: Int = 7,
    val skipFirstLap: Boolean = true,
    val startReadoutEnabled: Boolean = true,
    val startReadoutType: VehicleApproachStartReadoutType = VehicleApproachStartReadoutType.CAR_LEFT_RIGHT,
    val sustainedReadoutEnabled: Boolean = false,
    val sustainedReadoutType: VehicleApproachSustainedReadoutType = VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT,
)
