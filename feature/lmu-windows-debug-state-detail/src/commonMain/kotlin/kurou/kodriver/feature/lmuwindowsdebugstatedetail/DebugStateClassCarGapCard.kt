package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsClassCarGapData
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_class_car_gap_ahead
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_class_car_gap_behind
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ClassCarGapContent(classCarGap: LmuWindowsClassCarGapData?) {
    if (classCarGap == null) {
        DebugStateUnavailableContent()
        return
    }
    Column {
        Text(
            text =
                stringResource(
                    Res.string.debug_state_class_car_gap_ahead,
                    formatCarGapSeconds(classCarGap.gapAheadSeconds),
                ),
            style = koDriverNumericTextStyle(),
        )
        Text(
            text =
                stringResource(
                    Res.string.debug_state_class_car_gap_behind,
                    formatCarGapSeconds(classCarGap.gapBehindSeconds),
                ),
            style = koDriverNumericTextStyle(),
        )
    }
}
