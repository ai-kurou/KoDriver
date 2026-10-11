package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.AceWindowsStatusData

@Composable
internal fun VehicleLocationContent(aceWindowsStatus: AceWindowsStatusData?) {
    AceWindowsVehicleLocationContent(aceWindowsStatus)
}
