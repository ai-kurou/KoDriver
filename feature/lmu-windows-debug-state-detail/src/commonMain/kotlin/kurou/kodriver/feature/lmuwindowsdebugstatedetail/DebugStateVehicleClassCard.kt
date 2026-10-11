package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData

@Composable
internal fun VehicleClassContent(lmuWindowsVehicleClass: LmuWindowsVehicleClassData?) {
    val name = lmuWindowsVehicleClass?.name
    if (name.isNullOrEmpty()) {
        DebugStateUnavailableContent()
        return
    }
    Text(text = name)
}
