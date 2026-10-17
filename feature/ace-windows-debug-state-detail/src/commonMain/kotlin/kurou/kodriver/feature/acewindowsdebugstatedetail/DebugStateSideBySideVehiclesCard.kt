package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.runtime.Composable
import kurou.kodriver.domain.acewindows.model.AceWindowsVehicleApproachData

@Composable
internal fun SideBySideVehiclesContent(vehicleApproach: AceWindowsVehicleApproachData?) {
    AceWindowsNearbyVehiclesContent(vehicleApproach)
}
