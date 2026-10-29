package kurou.kodriver.domain.acewindows.model

import kotlinx.serialization.Serializable

/**
 * Assetto Corsa EVO の Windows 共有メモリから読み取った総合順位・総台数。
 * 0 は順位・台数が未確定であることを表す。
 */
@Serializable
data class AceWindowsOverallPositionData(
    /** 総合順位（1 始まり、未確定時は 0）。 */
    val position: Int,
    /** 総台数（未確定時は 0）。 */
    val totalDrivers: Int,
)
