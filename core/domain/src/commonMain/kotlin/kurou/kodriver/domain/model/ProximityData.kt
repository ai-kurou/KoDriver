package kurou.kodriver.domain.model

data class ProximityData(
    val sideBySideLeftVehicleIds: Set<Int>,
    val sideBySideRightVehicleIds: Set<Int>,
    // 並走していない場合は Double.MAX_VALUE
    val lateralDistanceLeftMeters: Double,
    val lateralDistanceRightMeters: Double,
) {
    val isSideBySideLeft: Boolean get() = sideBySideLeftVehicleIds.isNotEmpty()
    val isSideBySideRight: Boolean get() = sideBySideRightVehicleIds.isNotEmpty()
}
