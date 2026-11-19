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

/** ベストラップ時間（ms）を表示用に整形する。未計測（null）または0以下の場合は null を返す。 */
public fun formatBestLapTimeMsOrNull(bestLapTimeMs: Long?): String? =
    bestLapTimeMs?.takeIf { it > 0L }?.let { formatLapTimeMs(it) }
