package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.DebugStateHeadlineValueContent
import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.util.formatLapTimeMs

@Composable
internal fun BestLapContent(gt7Ps5Telemetry: Gt7Ps5TelemetryData?) {
    val bestLapTimeMs = gt7Ps5Telemetry?.bestLapTimeMs?.toLong()
    DebugStateHeadlineValueContent(bestLapTimeMs?.takeIf { it > 0L }?.let { formatLapTimeMs(it) })
}
