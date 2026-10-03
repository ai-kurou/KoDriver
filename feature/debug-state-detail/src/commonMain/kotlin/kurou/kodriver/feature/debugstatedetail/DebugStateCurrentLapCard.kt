package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.Simulator

@Composable
internal fun CurrentLapContent(
    selectedSimulator: Simulator,
    lmuWindowsTelemetry: LmuWindowsTelemetryData?,
    gt7Ps5Telemetry: Gt7Ps5TelemetryData?,
) {
    val currentLap =
        when (selectedSimulator) {
            is Simulator.LmuWindows -> lmuWindowsTelemetry?.timing?.currentLap
            is Simulator.Gt7Ps5 -> gt7Ps5Telemetry?.lapCount
            is Simulator.AceWindows -> null
        }
    val displayText = currentLap?.let { it.toString() }
    if (displayText == null) {
        DebugStateUnavailableContent()
    } else {
        Text(
            text = displayText,
            style = koDriverNumericTextStyle(MaterialTheme.typography.headlineMedium),
        )
    }
}
