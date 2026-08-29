package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

/**
 * Assetto Corsa EVO の Windows 共有メモリから読み取ったセッション中のベストラップタイム。
 */
@Serializable
data class AceWindowsBestLapTimeData(
    /** セッション中のベストラップタイム。単位は milliseconds。未計測時は 0 のことがある。 */
    val bestLapTimeMs: Int,
)
