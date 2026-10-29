package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsCarGapData
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_car_gap_car_ahead
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_car_gap_car_behind
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_car_gap_overall_ahead
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_car_gap_overall_behind
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_car_gap_place_ahead
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_car_gap_place_behind
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CarGapContent(carGap: LmuWindowsCarGapData?) {
    if (carGap == null) {
        DebugStateUnavailableContent()
        return
    }
    Column {
        Text(
            text =
                stringResource(
                    Res.string.debug_state_car_gap_overall_ahead,
                    formatCarGapSeconds(carGap.overallGapAheadSeconds),
                ),
            style = koDriverNumericTextStyle(),
        )
        Text(
            text =
                stringResource(
                    Res.string.debug_state_car_gap_overall_behind,
                    formatCarGapSeconds(carGap.overallGapBehindSeconds),
                ),
            style = koDriverNumericTextStyle(),
        )
        Text(
            text =
                stringResource(
                    Res.string.debug_state_car_gap_car_ahead,
                    formatCarGapSeconds(carGap.timeGapCarAheadSeconds),
                ),
            style = koDriverNumericTextStyle(),
        )
        Text(
            text =
                stringResource(
                    Res.string.debug_state_car_gap_car_behind,
                    formatCarGapSeconds(carGap.timeGapCarBehindSeconds),
                ),
            style = koDriverNumericTextStyle(),
        )
        Text(
            text =
                stringResource(
                    Res.string.debug_state_car_gap_place_ahead,
                    formatCarGapSeconds(carGap.timeGapPlaceAheadSeconds),
                ),
            style = koDriverNumericTextStyle(),
        )
        Text(
            text =
                stringResource(
                    Res.string.debug_state_car_gap_place_behind,
                    formatCarGapSeconds(carGap.timeGapPlaceBehindSeconds),
                ),
            style = koDriverNumericTextStyle(),
        )
    }
}
