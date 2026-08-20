package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

/**
 * GT7 の PS5 UDP パケットから抽出した読み上げ用テレメトリ。
 *
 * 燃料量はゲーム内の fuel unit をそのまま保持し、残量割合は
 * [gasLevel] / [gasCapacity] で求める。ラップタイムは milliseconds。
 */
@Serializable
data class Gt7Ps5TelemetryData(
    /** 現在の周回数。セッション切り替わり検出にも使う。 */
    val lapCount: Int,
    /** レースの総周回数。時間制や未設定の場合はゲーム側の値をそのまま保持する。 */
    val lapsInRace: Int,
    /** セッション中のベストラップタイム。単位は milliseconds。 */
    val bestLapTimeMs: Int,
    /** 現在の燃料残量。単位は GT7 の fuel unit。 */
    val gasLevel: Float,
    /** 燃料タンク容量。単位は GT7 の fuel unit。 */
    val gasCapacity: Float,
    /** 車両カテゴリ（例: "GR3", "GRX"）。取得できない場合は空文字列。 */
    val carCategory: String = "",
    /** 各タイヤの温度。取得できない場合は全て 0f。 */
    val tyreTemperature: Gt7Ps5TyreTemperatureData =
        Gt7Ps5TyreTemperatureData(
            CelsiusReading(0f),
            CelsiusReading(0f),
            CelsiusReading(0f),
            CelsiusReading(0f),
        ),
)
