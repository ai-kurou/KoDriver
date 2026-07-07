package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LmuWindowsVehicleDamageData(
    val overheating: Boolean,
    val partDetached: Boolean,
    val lastImpactMagnitude: Double,
)
