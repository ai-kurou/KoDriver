package kurou.kodriver.core.model

import kotlinx.serialization.Serializable

@Serializable
data class LmuWindowsVehicleDamageData(
    val overheating: Boolean,
    val partDetached: Boolean,
    val lastImpactMagnitude: Double,
)
