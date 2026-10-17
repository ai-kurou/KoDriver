package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5VehicleClassData

@Composable
internal fun VehicleClassContent(gt7Ps5VehicleClass: Gt7Ps5VehicleClassData?) {
    val name = gt7Ps5VehicleClass?.name
    if (name.isNullOrEmpty()) {
        DebugStateUnavailableContent()
        return
    }
    Text(text = name)
}
