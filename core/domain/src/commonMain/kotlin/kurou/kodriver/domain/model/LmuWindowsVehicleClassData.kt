package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LmuWindowsVehicleClassData(
    /** プレイヤー車両のクラス名（例: "Hypercar", "LMP2", "GTE", "LMGT3"）。 */
    val name: String,
)
