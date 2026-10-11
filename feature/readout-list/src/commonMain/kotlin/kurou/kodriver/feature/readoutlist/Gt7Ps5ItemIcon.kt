package kurou.kodriver.feature.readoutlist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey

internal fun gt7Ps5ItemIcon(itemId: Gt7Ps5ReadoutItemKey): ImageVector =
    when (itemId) {
        is Gt7Ps5ReadoutItemKey.MyBestLap -> Icons.Filled.Timer
        is Gt7Ps5ReadoutItemKey.RemainingFuelLaps -> Icons.Filled.LocalGasStation
        is Gt7Ps5ReadoutItemKey.RemainingFuel -> Icons.Filled.LocalGasStation
        is Gt7Ps5ReadoutItemKey.TyreTemperature -> Icons.Filled.DeviceThermostat
    }
