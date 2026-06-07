package kurou.kodriver.feature.readout.vehicleapproach

internal data class VehicleApproachUiState(
    val isSideBySideLeft: Boolean = false,
    val isSideBySideRight: Boolean = false,
    val lateralDistanceLeftMeters: Double? = null,
    val lateralDistanceRightMeters: Double? = null,
)
