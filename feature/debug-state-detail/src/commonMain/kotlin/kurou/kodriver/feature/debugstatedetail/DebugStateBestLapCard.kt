package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BestLapContent(
    selectedSimulator: Simulator,
    lmuWindowsTelemetry: LmuWindowsTelemetryData?,
    gt7Ps5Telemetry: Gt7Ps5TelemetryData?,
) {
    val bestLapTimeMs =
        when (selectedSimulator) {
            is Simulator.LmuWindows -> lmuWindowsTelemetry?.timing?.bestLapTimeMs
            is Simulator.Gt7Ps5 -> gt7Ps5Telemetry?.bestLapTimeMs?.toLong()
            is Simulator.AceWindows -> null
        }
    Text(
        text =
            bestLapTimeMs?.takeIf { it > 0L }?.let { formatLapTimeMs(it) }
                ?: stringResource(Res.string.debug_state_flag_info_unavailable),
    )
}
