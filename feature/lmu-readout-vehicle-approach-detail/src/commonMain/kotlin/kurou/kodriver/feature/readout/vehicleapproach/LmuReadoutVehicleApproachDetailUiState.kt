package kurou.kodriver.feature.readout.vehicleapproach

internal data class LmuReadoutVehicleApproachDetailUiState(
    val lateralThresholdMeters: Double = 5.0,
    val longitudinalThresholdMeters: Double = 1.0,
    val skipFirstLap: Boolean = true,
)
