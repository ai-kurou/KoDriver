package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle
import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5TelemetryData

@Composable
internal fun BestLapContent(gt7Ps5Telemetry: Gt7Ps5TelemetryData?) {
    val bestLapTimeMs = gt7Ps5Telemetry?.bestLapTimeMs?.toLong()
    val displayText = bestLapTimeMs?.takeIf { it > 0L }?.let { formatLapTimeMs(it) }
    if (displayText == null) {
        DebugStateUnavailableContent()
    } else {
        Text(
            text = displayText,
            style = koDriverNumericTextStyle(MaterialTheme.typography.headlineMedium),
        )
    }
}
