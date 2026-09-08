package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LmuWindowsTyreDetachedData(
    /** ホイールごとのタイヤ脱落有無。 */
    val wheels: Map<WheelIndex, Boolean>,
)
