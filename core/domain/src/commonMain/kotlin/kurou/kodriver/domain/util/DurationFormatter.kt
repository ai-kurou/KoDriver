package kurou.kodriver.domain.util

private const val MILLISECONDS_PER_SECOND = 1_000L
private const val MILLISECONDS_PER_MINUTE = 60 * MILLISECONDS_PER_SECOND
public const val MILLISECONDS_PER_HOUR: Long = 60 * MILLISECONDS_PER_MINUTE
public const val MILLISECONDS_PER_DAY: Long = 24 * MILLISECONDS_PER_HOUR

public data class DurationComponents(
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val millis: Long,
)

public fun decomposeDurationMs(milliseconds: Long): DurationComponents {
    val hours = milliseconds / MILLISECONDS_PER_HOUR
    val minutes = milliseconds % MILLISECONDS_PER_HOUR / MILLISECONDS_PER_MINUTE
    val seconds = milliseconds % MILLISECONDS_PER_MINUTE / MILLISECONDS_PER_SECOND
    val millis = milliseconds % MILLISECONDS_PER_SECOND
    return DurationComponents(hours, minutes, seconds, millis)
}

public fun Long.padStartZero(length: Int): String = toString().padStart(length, '0')

public fun formatLapTimeMs(milliseconds: Long): String {
    val totalMinutes = milliseconds / MILLISECONDS_PER_MINUTE
    val components = decomposeDurationMs(milliseconds)
    return "$totalMinutes:${components.seconds.padStartZero(2)}.${components.millis.padStartZero(3)}"
}

/**
 * ラップタイム（ms）を読み上げ用の日本語文言に整形する。
 * 分は合計の分（60以上もそのまま）、秒はゼロ埋めしない、ミリ秒は3桁にゼロ埋めする。
 * 分が0より大きければ「{分}分{秒}秒{ミリ秒}」、0なら「{秒}秒{ミリ秒}」の形式になる。
 */
public fun formatLapTimeReadoutMs(milliseconds: Long): String {
    val totalMinutes = milliseconds / MILLISECONDS_PER_MINUTE
    val components = decomposeDurationMs(milliseconds)
    val millis = components.millis.padStartZero(3)
    return if (totalMinutes > 0L) {
        "${totalMinutes}分${components.seconds}秒$millis"
    } else {
        "${components.seconds}秒$millis"
    }
}

/** ベストラップ時間（ms）を表示用に整形する。未計測（null）または0以下の場合は null を返す。 */
public fun formatBestLapTimeMsOrNull(bestLapTimeMs: Long?): String? =
    bestLapTimeMs?.takeIf { it > 0L }?.let { formatLapTimeMs(it) }
