package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

internal data class LmuWindowsReadoutVehicleApproachDetailUiState(
    val lateralThresholdMeters: Double = 5.0,
    val longitudinalThresholdMeters: Double = 1.0,
    val skipFirstLap: Boolean = true,
)
