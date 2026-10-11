package kurou.kodriver.feature.readoutlist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey

internal fun lmuWindowsItemIcon(itemId: LmuWindowsReadoutItemKey): ImageVector =
    when (itemId) {
        is LmuWindowsReadoutItemKey.VehicleApproach -> Icons.Filled.DirectionsCar
        is LmuWindowsReadoutItemKey.Flag -> Icons.Filled.Flag
        is LmuWindowsReadoutItemKey.VehicleDamage -> Icons.Filled.Build
        is LmuWindowsReadoutItemKey.TyreTemperature -> Icons.Filled.DeviceThermostat
        is LmuWindowsReadoutItemKey.PitTiming -> Icons.Filled.AccessTime
        is LmuWindowsReadoutItemKey.RemainingVirtualEnergy -> Icons.Filled.LocalGasStation
        is LmuWindowsReadoutItemKey.TyreWear -> Icons.Filled.DonutLarge
        is LmuWindowsReadoutItemKey.BrakeTemperature -> Icons.Filled.DeviceThermostat
        is LmuWindowsReadoutItemKey.BrakeWear -> Icons.Filled.Speed
        is LmuWindowsReadoutItemKey.MyBestLap -> Icons.Filled.Timer
    }
