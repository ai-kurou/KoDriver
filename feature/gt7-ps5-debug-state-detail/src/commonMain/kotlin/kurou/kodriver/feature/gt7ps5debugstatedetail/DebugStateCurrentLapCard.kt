package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.DebugStateHeadlineValueContent
import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5TelemetryData

@Composable
internal fun CurrentLapContent(gt7Ps5Telemetry: Gt7Ps5TelemetryData?) {
    DebugStateHeadlineValueContent(gt7Ps5Telemetry?.lapCount?.toString())
}
