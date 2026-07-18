package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LmuWindowsVirtualEnergyData(
    /** バーチャルエナジー残量割合（0.0〜1.0）。 */
    val remainingRatio: Double,
)
