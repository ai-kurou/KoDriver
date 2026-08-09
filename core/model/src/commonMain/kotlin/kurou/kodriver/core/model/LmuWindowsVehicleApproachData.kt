package kurou.kodriver.core.model

import kotlinx.serialization.Serializable

/**
 * LMU の周辺車両接近状態。
 *
 * 車両 ID の集合は、現在プレイヤー車両の左または右に並走している相手車両を表す。
 * 距離は meters。並走車両がいない側の距離は [Double.MAX_VALUE] で表現する。
 */
@Serializable
data class LmuWindowsVehicleApproachData(
    /** 左側で並走中の車両 ID。空なら左側に並走車両はいない。 */
    val sideBySideLeftVehicleIds: Set<Int>,
    /** 右側で並走中の車両 ID。空なら右側に並走車両はいない。 */
    val sideBySideRightVehicleIds: Set<Int>,
    /** 左側の最短横方向距離。単位は meters。並走していない場合は [Double.MAX_VALUE]。 */
    val lateralDistanceLeftMeters: Double,
    /** 右側の最短横方向距離。単位は meters。並走していない場合は [Double.MAX_VALUE]。 */
    val lateralDistanceRightMeters: Double,
) {
    /** 左側に並走車両が存在するかどうか。 */
    val isSideBySideLeft: Boolean get() = sideBySideLeftVehicleIds.isNotEmpty()

    /** 右側に並走車両が存在するかどうか。 */
    val isSideBySideRight: Boolean get() = sideBySideRightVehicleIds.isNotEmpty()
}
