package kurou.kodriver.domain.util

const val MILLISECONDS_PER_SECOND = 1_000L
const val MILLISECONDS_PER_MINUTE = 60 * MILLISECONDS_PER_SECOND
const val MILLISECONDS_PER_HOUR = 60 * MILLISECONDS_PER_MINUTE
const val MILLISECONDS_PER_DAY = 24 * MILLISECONDS_PER_HOUR

data class DurationComponents(
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val millis: Long,
)

fun decomposeDurationMs(milliseconds: Long): DurationComponents {
    val hours = milliseconds / MILLISECONDS_PER_HOUR
    val minutes = milliseconds % MILLISECONDS_PER_HOUR / MILLISECONDS_PER_MINUTE
    val seconds = milliseconds % MILLISECONDS_PER_MINUTE / MILLISECONDS_PER_SECOND
    val millis = milliseconds % MILLISECONDS_PER_SECOND
    return DurationComponents(hours, minutes, seconds, millis)
}

fun Long.padStartZero(length: Int): String = toString().padStart(length, '0')
