package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

/**
 * Assetto Corsa EVO の Windows 共有メモリから読み取った燃料残量。
 */
@Serializable
data class AceWindowsFuelData(
    /** 燃料残量割合。単位は percent。0.0 より大きく 100.0 以下の値を想定する。 */
    val remainingPercent: Double,
)
