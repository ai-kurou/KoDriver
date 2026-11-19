package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.DebugStateHeadlineValueContent
import kurou.kodriver.domain.acewindows.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.util.formatBestLapTimeMsOrNull

@Composable
internal fun BestLapContent(aceWindowsBestLapTime: AceWindowsBestLapTimeData?) {
    val bestLapTimeMs = aceWindowsBestLapTime?.bestLapTimeMs?.toLong()
    DebugStateHeadlineValueContent(formatBestLapTimeMsOrNull(bestLapTimeMs))
}
