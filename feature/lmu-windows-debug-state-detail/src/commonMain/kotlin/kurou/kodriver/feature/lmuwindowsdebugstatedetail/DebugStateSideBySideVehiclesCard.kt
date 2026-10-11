package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData

@Composable
internal fun SideBySideVehiclesContent(
    vehicleApproach: LmuWindowsVehicleApproachData?,
    durations: LmuWindowsSideBySideDurations?,
) {
    LmuWindowsSideBySideVehiclesContent(vehicleApproach, durations)
}
