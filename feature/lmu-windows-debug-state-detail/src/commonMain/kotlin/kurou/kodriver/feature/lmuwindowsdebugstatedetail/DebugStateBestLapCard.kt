package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.DebugStateHeadlineValueContent
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.util.formatLapTimeMs

@Composable
internal fun BestLapContent(lmuWindowsTelemetry: LmuWindowsTelemetryData?) {
    val bestLapTimeMs = lmuWindowsTelemetry?.timing?.bestLapTimeMs
    DebugStateHeadlineValueContent(bestLapTimeMs?.takeIf { it > 0L }?.let { formatLapTimeMs(it) })
}
