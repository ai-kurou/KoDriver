package kurou.kodriver.domain.lmuwindows.model

import kotlinx.serialization.Serializable

@Serializable
public data class LmuWindowsVehicleDamageData(
    val overheating: Boolean,
    val partDetached: Boolean,
    val lastImpactMagnitude: Double,
)
