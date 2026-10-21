package kurou.kodriver.domain.lmuwindows.model

import kotlinx.serialization.Serializable
import kurou.kodriver.domain.model.WheelIndex

@Serializable
data class LmuWindowsTyreDetachedData(
    /** ホイールごとのタイヤ脱落有無。 */
    val wheels: Map<WheelIndex, Boolean>,
)
