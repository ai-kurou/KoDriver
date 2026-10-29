package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle
import kurou.kodriver.domain.acewindows.model.AceWindowsGapData
import kurou.kodriver.domain.acewindows.model.AceWindowsOverallPositionData
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.Res
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_car_gap_car_ahead
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_car_gap_car_behind
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_car_gap_early_access_note
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_car_gap_overall_position
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CarGapContent(
    gap: AceWindowsGapData?,
    overallPosition: AceWindowsOverallPositionData?,
) {
    if (gap == null && overallPosition == null) {
        DebugStateUnavailableContent()
        return
    }
    Column {
        Text(
            text =
                stringResource(
                    Res.string.debug_state_car_gap_overall_position,
                    formatOverallPosition(overallPosition?.position),
                    formatOverallPosition(overallPosition?.totalDrivers),
                ),
            style = koDriverNumericTextStyle(),
        )
        Text(
            text =
                stringResource(
                    Res.string.debug_state_car_gap_car_ahead,
                    gap?.let { formatCarGapSeconds(it.gapAheadSeconds) } ?: "--",
                ),
            style = koDriverNumericTextStyle(),
        )
        Text(
            text =
                stringResource(
                    Res.string.debug_state_car_gap_car_behind,
                    gap?.let { formatCarGapSeconds(it.gapBehindSeconds) } ?: "--",
                ),
            style = koDriverNumericTextStyle(),
        )
        Text(
            text = stringResource(Res.string.debug_state_car_gap_early_access_note),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
