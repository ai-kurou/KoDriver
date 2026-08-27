package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.Gt7Ps5VehicleClassData
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun VehicleClassContent(
    selectedSimulator: Simulator,
    lmuWindowsVehicleClass: LmuWindowsVehicleClassData?,
    gt7Ps5VehicleClass: Gt7Ps5VehicleClassData?,
) {
    val name =
        when (selectedSimulator) {
            is Simulator.LmuWindows -> lmuWindowsVehicleClass?.name
            is Simulator.Gt7Ps5 -> gt7Ps5VehicleClass?.name
            is Simulator.AceWindows -> null
        }
    if (name.isNullOrEmpty()) {
        Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
        return
    }
    Text(text = name)
}
