package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_carcass_temperature_fl
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_carcass_temperature_fr
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_carcass_temperature_rl
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_carcass_temperature_rr
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val WHEEL_COLUMN_WIDTH = 110.dp

@Composable
internal fun TyreCarcassTemperatureContent(
    selectedSimulator: Simulator?,
    tyreCarcassTemperature: LmuWindowsTyreCarcassTemperatureData?,
    aceWindowsTyreCarcassTemperature: AceWindowsTyreCarcassTemperatureData?,
) {
    val wheels =
        when (selectedSimulator) {
            is Simulator.LmuWindows -> tyreCarcassTemperature?.wheels
            is Simulator.AceWindows -> aceWindowsTyreCarcassTemperature?.wheels
            is Simulator.Gt7Ps5, null -> null
        }
    if (wheels == null) {
        Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
        return
    }
    Column {
        Row {
            WheelCarcassTemperatureText(
                wheels,
                WheelIndex.FRONT_LEFT,
                Res.string.debug_state_tyre_carcass_temperature_fl,
            )
            WheelCarcassTemperatureText(
                wheels,
                WheelIndex.FRONT_RIGHT,
                Res.string.debug_state_tyre_carcass_temperature_fr,
            )
        }
        Row {
            WheelCarcassTemperatureText(
                wheels,
                WheelIndex.REAR_LEFT,
                Res.string.debug_state_tyre_carcass_temperature_rl,
            )
            WheelCarcassTemperatureText(
                wheels,
                WheelIndex.REAR_RIGHT,
                Res.string.debug_state_tyre_carcass_temperature_rr,
            )
        }
    }
}

@Composable
private fun WheelCarcassTemperatureText(
    wheels: Map<WheelIndex, CelsiusReading>,
    wheelIndex: WheelIndex,
    labelRes: StringResource,
) {
    Text(
        text = stringResource(labelRes, wheelCarcassTemperatureText(wheels, wheelIndex)),
        modifier = Modifier.width(WHEEL_COLUMN_WIDTH),
        softWrap = false,
    )
}
