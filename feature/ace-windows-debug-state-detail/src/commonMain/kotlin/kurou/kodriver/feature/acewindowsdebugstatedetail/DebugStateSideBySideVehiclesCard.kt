package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.AceWindowsVehicleApproachData

@Composable
internal fun SideBySideVehiclesContent(vehicleApproach: AceWindowsVehicleApproachData?) {
    AceWindowsNearbyVehiclesContent(vehicleApproach)
}
