package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LmuWindowsVehicleClassData(
    /** プレイヤー車両のクラス名（2026年8月時点の実測値: "GT3", "GTE", "LMP3", "LMP2", "LMP2_ELMS", "Hyper"）。 */
    val name: String,
)
