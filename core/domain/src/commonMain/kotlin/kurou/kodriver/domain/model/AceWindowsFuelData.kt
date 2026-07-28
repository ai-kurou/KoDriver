package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AceWindowsFuelData(
    val remainingPercent: Double,
)
