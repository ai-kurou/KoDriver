package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LmuWindowsTyreWearData(
    /** ホイールごとの残タイヤ溝割合（0.0-1.0。1.0=新品、0.0=摩耗限界）。 */
    val wheels: Map<WheelIndex, Double>,
)
