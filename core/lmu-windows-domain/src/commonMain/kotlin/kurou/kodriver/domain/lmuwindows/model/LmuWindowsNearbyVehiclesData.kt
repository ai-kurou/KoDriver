package kurou.kodriver.domain.lmuwindows.model

import kotlinx.serialization.Serializable

/**
 * LMU の自車周辺にいる車両の、閾値を適用していない位置情報。
 *
 * 並走かどうかの判定は読み上げ・表示を行う端末の閾値で行うため、ここでは判定しない。
 * 車両接近の閾値の設定可能な上限より遠い車両は含まない。
 */
@Serializable
public data class LmuWindowsNearbyVehiclesData(
    /** 自車以外の周辺車両。 */
    val vehicles: List<LmuWindowsNearbyVehicleData>,
)

/** LMU の周辺車両1台分の、自車の向きを基準にした位置。単位は meters。 */
@Serializable
public data class LmuWindowsNearbyVehicleData(
    /** 車両 ID。接近継続時間を車両ごとに追跡するキーに使う。 */
    val vehicleId: Int,
    /** 左右方向の位置。負が左、正が右。 */
    val lateralMeters: Double,
    /** 前後方向の位置。 */
    val longitudinalMeters: Double,
)
