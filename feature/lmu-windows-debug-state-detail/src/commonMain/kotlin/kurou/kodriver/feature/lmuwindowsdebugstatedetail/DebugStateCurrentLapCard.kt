package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.DebugStateHeadlineValueContent
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData

@Composable
internal fun CurrentLapContent(lmuWindowsTelemetry: LmuWindowsTelemetryData?) {
    DebugStateHeadlineValueContent(lmuWindowsTelemetry?.timing?.currentLap?.toString())
}
