package kurou.kodriver.domain.model

data class ProximityData(
    val isSideBySideLeft: Boolean,
    val isSideBySideRight: Boolean,
    // 並走していない場合は Double.MAX_VALUE
    val lateralDistanceLeftMeters: Double,
    val lateralDistanceRightMeters: Double,
)
