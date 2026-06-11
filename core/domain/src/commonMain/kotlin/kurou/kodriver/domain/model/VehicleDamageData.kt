package kurou.kodriver.domain.model

data class VehicleDamageData(
    val overheating: Boolean,
    val partDetached: Boolean,
    val lastImpactMagnitude: Double,
)
