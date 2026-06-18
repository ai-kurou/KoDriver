package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import kurou.kodriver.domain.model.VehicleApproachStartReadoutType

internal data class LmuWindowsReadoutVehicleApproachDetailUiState(
    val lateralThresholdMeters: Double = 5.0,
    val longitudinalThresholdMeters: Double = 1.0,
    val skipFirstLap: Boolean = true,
    val startReadoutEnabled: Boolean = true,
    val startReadoutType: VehicleApproachStartReadoutType = VehicleApproachStartReadoutType.CAR_LEFT_RIGHT,
)
