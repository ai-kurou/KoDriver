package kurou.kodriver.domain.lmuwindows.model

import kotlinx.serialization.Serializable

@Serializable
data class LmuWindowsFuelData(
    val currentLiters: LmuWindowsFuelUnit,
    val capacityLiters: LmuWindowsFuelUnit,
)
