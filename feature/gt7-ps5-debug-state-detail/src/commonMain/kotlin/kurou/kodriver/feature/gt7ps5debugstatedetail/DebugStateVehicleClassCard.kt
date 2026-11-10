package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.DebugStateTextValueContent
import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5VehicleClassData

@Composable
internal fun VehicleClassContent(gt7Ps5VehicleClass: Gt7Ps5VehicleClassData?) {
    DebugStateTextValueContent(gt7Ps5VehicleClass?.name)
}
