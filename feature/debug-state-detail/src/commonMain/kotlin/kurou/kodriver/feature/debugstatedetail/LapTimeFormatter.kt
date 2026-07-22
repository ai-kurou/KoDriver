package kurou.kodriver.feature.debugstatedetail

private const val MILLISECONDS_PER_SECOND = 1_000L
private const val MILLISECONDS_PER_MINUTE = 60 * MILLISECONDS_PER_SECOND

internal fun formatLapTimeMs(milliseconds: Long): String {
    val minutes = milliseconds / MILLISECONDS_PER_MINUTE
    val seconds = milliseconds % MILLISECONDS_PER_MINUTE / MILLISECONDS_PER_SECOND
    val millis = milliseconds % MILLISECONDS_PER_SECOND
    return "$minutes:${seconds.pad2()}.${millis.pad3()}"
}

private fun Long.pad2(): String = toString().padStart(2, '0')

private fun Long.pad3(): String = toString().padStart(3, '0')
