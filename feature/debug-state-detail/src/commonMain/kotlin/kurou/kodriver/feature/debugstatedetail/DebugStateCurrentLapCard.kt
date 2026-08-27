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
    Text(
        text = currentLap?.toString() ?: stringResource(Res.string.debug_state_flag_info_unavailable),
    )
}
