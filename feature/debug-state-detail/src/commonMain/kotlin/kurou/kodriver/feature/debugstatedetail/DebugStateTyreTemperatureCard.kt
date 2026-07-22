package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kodriver.feature.debugstatedetail.generated.resources.Res
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_temperature_fl
import kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_temperature_fr
import kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_temperature_rl
import kodriver.feature.debugstatedetail.generated.resources.debug_state_tyre_temperature_rr
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TyreTemperatureContent(
    selectedSimulator: Simulator?,
    lmuWindowsTelemetry: LmuWindowsTelemetryData?,
) {
    val wheels = lmuWindowsTelemetry?.tyres?.wheels
    if (selectedSimulator !is Simulator.LmuWindows || wheels == null) {
        Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
        return
    }
    Column {
        Text(
            text = stringResource(
                Res.string.debug_state_tyre_temperature_fl,
                wheelTemperatureText(wheels, WheelIndex.FRONT_LEFT),
            ),
        )
        Text(
            text = stringResource(
                Res.string.debug_state_tyre_temperature_fr,
                wheelTemperatureText(wheels, WheelIndex.FRONT_RIGHT),
            ),
        )
        Text(
            text = stringResource(
                Res.string.debug_state_tyre_temperature_rl,
                wheelTemperatureText(wheels, WheelIndex.REAR_LEFT),
            ),
        )
        Text(
            text = stringResource(
                Res.string.debug_state_tyre_temperature_rr,
                wheelTemperatureText(wheels, WheelIndex.REAR_RIGHT),
            ),
        )
    }
}
