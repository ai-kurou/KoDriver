package kurou.kodriver.feature.debugstatedetail

import kurou.kodriver.domain.util.MILLISECONDS_PER_MINUTE
import kurou.kodriver.domain.util.decomposeDurationMs
import kurou.kodriver.domain.util.padStartZero

internal fun formatLapTimeMs(milliseconds: Long): String {
    val totalMinutes = milliseconds / MILLISECONDS_PER_MINUTE
    val components = decomposeDurationMs(milliseconds)
    return "$totalMinutes:${components.seconds.padStartZero(2)}.${components.millis.padStartZero(3)}"
}
