package kurou.kodriver.feature.lmureadout.vehicleapproachdetail

internal data class LmuWindowsReadoutVehicleApproachDetailUiState(
    val lateralThresholdMeters: Double = 5.0,
    val longitudinalThresholdMeters: Double = 1.0,
    val skipFirstLap: Boolean = true,
)
