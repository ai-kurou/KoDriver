package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.DebugStateTextValueContent
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData

@Composable
internal fun VehicleClassContent(lmuWindowsVehicleClass: LmuWindowsVehicleClassData?) {
    DebugStateTextValueContent(lmuWindowsVehicleClass?.name)
}
