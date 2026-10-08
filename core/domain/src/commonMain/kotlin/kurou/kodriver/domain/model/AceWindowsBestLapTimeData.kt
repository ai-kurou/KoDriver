package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

/**
 * Assetto Corsa EVO の Windows 共有メモリから読み取ったセッション中のベストラップタイム。
 */
@Serializable
data class AceWindowsBestLapTimeData(
    /** セッション中のベストラップタイム。単位は milliseconds。未計測時は 0 のことがある。 */
    val bestLapTimeMs: Int,
    /** セッション状態（`session_state`）の現在ラップ番号。新しいセッションの検出に使う。旧バージョンのサーバーからは配信されないため既定値は 0。 */
    val currentLap: Int = 0,
)
