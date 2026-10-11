package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle
import kurou.kodriver.domain.model.AceWindowsBestLapTimeData

@Composable
internal fun BestLapContent(aceWindowsBestLapTime: AceWindowsBestLapTimeData?) {
    val bestLapTimeMs = aceWindowsBestLapTime?.bestLapTimeMs?.toLong()
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
