package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_wear_fl
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_wear_fr
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_wear_rl
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_wear_rr
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val WHEEL_COLUMN_WIDTH = 110.dp

@Composable
internal fun TyreWearContent(
    selectedSimulator: Simulator,
    lmuWindowsTelemetry: LmuWindowsTelemetryData?,
) {
    val wheels = lmuWindowsTelemetry?.tyres?.wheels
    if (selectedSimulator !is Simulator.LmuWindows || wheels == null) {
        Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
        return
    }
    Column {
        Row {
            WheelWearText(wheels, WheelIndex.FRONT_LEFT, Res.string.debug_state_tyre_wear_fl)
            WheelWearText(wheels, WheelIndex.FRONT_RIGHT, Res.string.debug_state_tyre_wear_fr)
        }
        Row {
            WheelWearText(wheels, WheelIndex.REAR_LEFT, Res.string.debug_state_tyre_wear_rl)
            WheelWearText(wheels, WheelIndex.REAR_RIGHT, Res.string.debug_state_tyre_wear_rr)
        }
    }
}

// デバッグ表示専用の固定4輪分Mapのため、ImmutableMap化のコストに見合わない。
@Suppress("UnstableCollections")
@Composable
private fun WheelWearText(
    wheels: Map<WheelIndex, LmuWindowsTyreWheelData>,
    wheelIndex: WheelIndex,
    labelRes: StringResource,
) {
    Text(
        text = stringResource(labelRes, wheelWearPercentText(wheels, wheelIndex)),
        modifier = Modifier.width(WHEEL_COLUMN_WIDTH),
        softWrap = false,
        style = koDriverNumericTextStyle(),
    )
}
