package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle
import kurou.kodriver.domain.acewindows.model.AceWindowsGapData
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.Res
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_car_gap_car_ahead
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_car_gap_car_behind
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CarGapContent(gap: AceWindowsGapData?) {
    if (gap == null) {
        DebugStateUnavailableContent()
        return
    }
    Column {
        Text(
            text =
                stringResource(
                    Res.string.debug_state_car_gap_car_ahead,
                    formatCarGapSeconds(gap.gapAheadSeconds),
                ),
            style = koDriverNumericTextStyle(),
        )
        Text(
            text =
                stringResource(
                    Res.string.debug_state_car_gap_car_behind,
                    formatCarGapSeconds(gap.gapBehindSeconds),
                ),
            style = koDriverNumericTextStyle(),
        )
    }
}
