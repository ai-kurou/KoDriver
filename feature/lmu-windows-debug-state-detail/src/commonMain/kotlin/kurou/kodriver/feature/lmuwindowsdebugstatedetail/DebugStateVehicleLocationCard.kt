package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.runtime.Composable
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsPitStatusData

@Composable
internal fun VehicleLocationContent(lmuWindowsPitStatus: LmuWindowsPitStatusData?) {
    LmuWindowsVehicleLocationContent(lmuWindowsPitStatus)
}
