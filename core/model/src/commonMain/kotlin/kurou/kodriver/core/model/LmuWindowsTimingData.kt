package kurou.kodriver.core.model

import kotlinx.serialization.Serializable

/**
 * LMU のラップタイムと周回情報。
 *
 * 時刻系はすべて milliseconds。Scoring のプレイヤー車両が見つからない場合など、
 * LMU から有効な値を取得できない項目は 0 として流れることがある。
 */
@Serializable
data class LmuWindowsTimingData(
    /** 現在ラップの経過時間。単位は milliseconds。 */
    val currentLapTimeMs: Long,
    /** 直前に完了したラップタイム。単位は milliseconds。 */
    val lastLapTimeMs: Long,
    /** セッション中のベストラップタイム。単位は milliseconds。 */
    val bestLapTimeMs: Long,
    /** ベストラップのセクター 1 タイム。単位は milliseconds。 */
    val sector1Ms: Long,
    /** ベストラップのセクター 1 + 2 の合計タイム。単位は milliseconds。 */
    val sector1And2Ms: Long,
    /** 現在の周回番号。LMU shared memory の値をそのまま扱う。 */
    val currentLap: Int,
    /** セッションの最大周回数。時間制レースなどで未設定の場合は 0 のことがある。 */
    val maxLaps: Int,
)
