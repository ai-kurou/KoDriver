package kurou.kodriver.feature.debugstatedetail

import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.Simulator

@Composable
internal fun SideBySideVehiclesContent(
    selectedSimulator: Simulator,
    vehicleApproach: LmuWindowsVehicleApproachData?,
    aceWindowsVehicleApproach: AceWindowsVehicleApproachData?,
    durations: LmuWindowsSideBySideDurations?,
) {
    when (selectedSimulator) {
        is Simulator.LmuWindows -> LmuWindowsSideBySideVehiclesContent(vehicleApproach, durations)
        is Simulator.AceWindows -> AceWindowsNearbyVehiclesContent(aceWindowsVehicleApproach)
        is Simulator.Gt7Ps5 -> DebugStateUnavailableContent()
    }
}
