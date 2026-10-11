package kurou.kodriver.feature.readoutlist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey

internal fun aceWindowsItemIcon(itemId: AceWindowsReadoutItemKey): ImageVector =
    when (itemId) {
        is AceWindowsReadoutItemKey.VehicleApproach -> Icons.Filled.DirectionsCar
        is AceWindowsReadoutItemKey.Flag -> Icons.Filled.Flag
        is AceWindowsReadoutItemKey.RemainingFuel -> Icons.Filled.LocalGasStation
        is AceWindowsReadoutItemKey.RemainingFuelLaps -> Icons.Filled.LocalGasStation
        is AceWindowsReadoutItemKey.TyreTemperature -> Icons.Filled.DeviceThermostat
        is AceWindowsReadoutItemKey.MyBestLap -> Icons.Filled.Timer
    }
