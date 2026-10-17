package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData

@Composable
internal fun BestLapContent(lmuWindowsTelemetry: LmuWindowsTelemetryData?) {
    val bestLapTimeMs = lmuWindowsTelemetry?.timing?.bestLapTimeMs
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
