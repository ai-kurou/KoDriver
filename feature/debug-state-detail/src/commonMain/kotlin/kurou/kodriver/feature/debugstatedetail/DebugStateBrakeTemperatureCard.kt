package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_brake_temperature_fl
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_brake_temperature_fr
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_brake_temperature_rl
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_brake_temperature_rr
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val WHEEL_COLUMN_WIDTH = 110.dp

@Composable
internal fun BrakeTemperatureContent(
    selectedSimulator: Simulator,
    brakeTemperature: LmuWindowsBrakeTemperatureData?,
) {
    val wheels =
        when (selectedSimulator) {
            is Simulator.LmuWindows -> brakeTemperature?.wheels
            is Simulator.AceWindows, is Simulator.Gt7Ps5 -> null
        }
    if (wheels == null) {
        Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
        return
    }
    Column {
        Row {
            WheelBrakeTemperatureText(wheels, WheelIndex.FRONT_LEFT, Res.string.debug_state_brake_temperature_fl)
            WheelBrakeTemperatureText(wheels, WheelIndex.FRONT_RIGHT, Res.string.debug_state_brake_temperature_fr)
        }
        Row {
            WheelBrakeTemperatureText(wheels, WheelIndex.REAR_LEFT, Res.string.debug_state_brake_temperature_rl)
            WheelBrakeTemperatureText(wheels, WheelIndex.REAR_RIGHT, Res.string.debug_state_brake_temperature_rr)
        }
    }
}

// デバッグ表示専用の固定4輪分Mapのため、ImmutableMap化のコストに見合わない。
@Suppress("UnstableCollections")
@Composable
private fun WheelBrakeTemperatureText(
    wheels: Map<WheelIndex, CelsiusReading>,
    wheelIndex: WheelIndex,
    labelRes: StringResource,
) {
    Text(
        text = stringResource(labelRes, wheelCarcassTemperatureText(wheels, wheelIndex)),
        modifier = Modifier.width(WHEEL_COLUMN_WIDTH),
        softWrap = false,
        style = koDriverNumericTextStyle(),
    )
}
