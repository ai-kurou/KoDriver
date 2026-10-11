package kurou.kodriver.feature.debugstatedetail

import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.model.Simulator

@Composable
internal fun VehicleLocationContent(
    selectedSimulator: Simulator,
    aceWindowsStatus: AceWindowsStatusData?,
    lmuWindowsPitStatus: LmuWindowsPitStatusData?,
) {
    when (selectedSimulator) {
        is Simulator.AceWindows -> {
            AceWindowsVehicleLocationContent(aceWindowsStatus)
        }

        is Simulator.LmuWindows -> {
            LmuWindowsVehicleLocationContent(lmuWindowsPitStatus)
        }

        is Simulator.Gt7Ps5 -> {
            DebugStateUnavailableContent()
        }
    }
}
