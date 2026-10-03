package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle
import kurou.kodriver.domain.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.Simulator

@Composable
internal fun BestLapContent(
    selectedSimulator: Simulator,
    lmuWindowsTelemetry: LmuWindowsTelemetryData?,
    gt7Ps5Telemetry: Gt7Ps5TelemetryData?,
    aceWindowsBestLapTime: AceWindowsBestLapTimeData?,
) {
    val bestLapTimeMs =
        when (selectedSimulator) {
            is Simulator.LmuWindows -> lmuWindowsTelemetry?.timing?.bestLapTimeMs
            is Simulator.Gt7Ps5 -> gt7Ps5Telemetry?.bestLapTimeMs?.toLong()
            is Simulator.AceWindows -> aceWindowsBestLapTime?.bestLapTimeMs?.toLong()
        }
    val displayText = bestLapTimeMs?.takeIf { it > 0L }?.let { formatLapTimeMs(it) }
    if (displayText == null) {
        DebugStateUnavailableContent()
    } else {
        Text(
            text = displayText,
            style = koDriverNumericTextStyle().copy(fontSize = MaterialTheme.typography.headlineMedium.fontSize),
        )
    }
}
