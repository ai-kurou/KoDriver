package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

/**
 * Assetto Corsa EVO の周辺車両との位置関係。
 *
 * ACE の Graphics 共有メモリには他車のワールド座標のみが含まれ、自車の向きに相当する
 * フィールドが存在しないため、LMU（[LmuWindowsVehicleApproachData]）のような左右の
 * 並走判定はできない。ここでは自車とのワールド座標上の直線距離のみを提供する。
 */
@Serializable
data class AceWindowsVehicleApproachData(
    /** 自車以外の各アクティブ車両との位置関係。 */
    val nearbyVehicles: List<AceWindowsNearbyVehicleData>,
)

/** ACE の周辺車両1台分の、自車から見た位置関係。 */
@Serializable
data class AceWindowsNearbyVehicleData(
    /** 自車とのワールド座標上の直線距離。単位は meters。 */
    val distanceMeters: Double,
)
