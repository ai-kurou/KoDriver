package kurou.kodriver.feature.readoutlist

import androidx.compose.runtime.Composable
import kodriver.feature.readoutlist.generated.resources.Res
import kodriver.feature.readoutlist.generated.resources.item_ace_black_flag
import kodriver.feature.readoutlist.generated.resources.item_ace_black_white_flag
import kodriver.feature.readoutlist.generated.resources.item_ace_blue_flag
import kodriver.feature.readoutlist.generated.resources.item_ace_checkered_flag
import kodriver.feature.readoutlist.generated.resources.item_ace_green_flag
import kodriver.feature.readoutlist.generated.resources.item_ace_orange_circle_flag
import kodriver.feature.readoutlist.generated.resources.item_ace_red_flag
import kodriver.feature.readoutlist.generated.resources.item_ace_red_yellow_stripes_flag
import kodriver.feature.readoutlist.generated.resources.item_ace_white_flag
import kodriver.feature.readoutlist.generated.resources.item_ace_yellow_flag
import kodriver.feature.readoutlist.generated.resources.item_blue_flag
import kodriver.feature.readoutlist.generated.resources.item_flag
import kodriver.feature.readoutlist.generated.resources.item_full_course_yellow
import kodriver.feature.readoutlist.generated.resources.item_my_best_lap
import kodriver.feature.readoutlist.generated.resources.item_overheat
import kodriver.feature.readoutlist.generated.resources.item_pit_timing
import kodriver.feature.readoutlist.generated.resources.item_red_flag
import kodriver.feature.readoutlist.generated.resources.item_remaining_fuel
import kodriver.feature.readoutlist.generated.resources.item_remaining_fuel_laps
import kodriver.feature.readoutlist.generated.resources.item_remaining_virtual_energy
import kodriver.feature.readoutlist.generated.resources.item_sector_yellow_flag
import kodriver.feature.readoutlist.generated.resources.item_tyre_low_warning
import kodriver.feature.readoutlist.generated.resources.item_tyre_overheat_warning
import kodriver.feature.readoutlist.generated.resources.item_tyre_temperature
import kodriver.feature.readoutlist.generated.resources.item_tyre_wear
import kodriver.feature.readoutlist.generated.resources.item_vehicle_approach
import kodriver.feature.readoutlist.generated.resources.item_vehicle_approach_start_readout
import kodriver.feature.readoutlist.generated.resources.item_vehicle_approach_sustained
import kodriver.feature.readoutlist.generated.resources.item_vehicle_damage
import kurou.kodriver.domain.model.ReadoutItemKey
import org.jetbrains.compose.resources.stringResource

@Composable
private fun flagItemDisplayName(flag: ReadoutItemKey.LmuWindows.Flag): String = when (flag) {
    is ReadoutItemKey.LmuWindows.Flag.Root -> stringResource(Res.string.item_flag)
    is ReadoutItemKey.LmuWindows.Flag.BlueFlag -> stringResource(Res.string.item_blue_flag)
    is ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag -> stringResource(Res.string.item_sector_yellow_flag)
    is ReadoutItemKey.LmuWindows.Flag.FullCourseYellow -> stringResource(Res.string.item_full_course_yellow)
    is ReadoutItemKey.LmuWindows.Flag.RedFlag -> stringResource(Res.string.item_red_flag)
}

@Composable
private fun aceFlagItemDisplayName(flag: ReadoutItemKey.AceWindows.Flag): String = when (flag) {
    is ReadoutItemKey.AceWindows.Flag.Root -> {
        stringResource(Res.string.item_flag)
    }
    is ReadoutItemKey.AceWindows.Flag.WhiteFlag -> {
        stringResource(Res.string.item_ace_white_flag)
    }
    is ReadoutItemKey.AceWindows.Flag.GreenFlag -> {
        stringResource(Res.string.item_ace_green_flag)
    }
    is ReadoutItemKey.AceWindows.Flag.RedFlag -> {
        stringResource(Res.string.item_ace_red_flag)
    }
    is ReadoutItemKey.AceWindows.Flag.BlueFlag -> {
        stringResource(Res.string.item_ace_blue_flag)
    }
    is ReadoutItemKey.AceWindows.Flag.YellowFlag -> {
        stringResource(Res.string.item_ace_yellow_flag)
    }
    is ReadoutItemKey.AceWindows.Flag.BlackFlag -> {
        stringResource(Res.string.item_ace_black_flag)
    }
    is ReadoutItemKey.AceWindows.Flag.BlackWhiteFlag -> {
        stringResource(Res.string.item_ace_black_white_flag)
    }
    is ReadoutItemKey.AceWindows.Flag.CheckeredFlag -> {
        stringResource(Res.string.item_ace_checkered_flag)
    }
    is ReadoutItemKey.AceWindows.Flag.OrangeCircleFlag -> {
        stringResource(Res.string.item_ace_orange_circle_flag)
    }
    is ReadoutItemKey.AceWindows.Flag.RedYellowStripesFlag -> {
        stringResource(Res.string.item_ace_red_yellow_stripes_flag)
    }
}

@Composable
private fun vehicleApproachItemDisplayName(vehicleApproach: ReadoutItemKey.LmuWindows.VehicleApproach): String =
    when (vehicleApproach) {
        is ReadoutItemKey.LmuWindows.VehicleApproach.Root -> {
            stringResource(Res.string.item_vehicle_approach)
        }
        is ReadoutItemKey.LmuWindows.VehicleApproach.Sustained -> {
            stringResource(Res.string.item_vehicle_approach_sustained)
        }
        is ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout -> {
            stringResource(Res.string.item_vehicle_approach_start_readout)
        }
    }

@Composable
private fun tyreTemperatureItemDisplayName(tyreTemperature: ReadoutItemKey.LmuWindows.TyreTemperature): String =
    when (tyreTemperature) {
        is ReadoutItemKey.LmuWindows.TyreTemperature.Root -> {
            stringResource(Res.string.item_tyre_temperature)
        }
        is ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning -> {
            stringResource(Res.string.item_tyre_overheat_warning)
        }
        is ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning -> {
            stringResource(Res.string.item_tyre_low_warning)
        }
    }

@Composable
private fun vehicleDamageItemDisplayName(vehicleDamage: ReadoutItemKey.LmuWindows.VehicleDamage): String =
    when (vehicleDamage) {
        is ReadoutItemKey.LmuWindows.VehicleDamage.Root -> stringResource(Res.string.item_vehicle_damage)
        is ReadoutItemKey.LmuWindows.VehicleDamage.Overheat -> stringResource(Res.string.item_overheat)
    }

@Composable
internal fun itemDisplayName(itemId: ReadoutItemKey): String = when (itemId) {
    is ReadoutItemKey.LmuWindows.VehicleApproach -> {
        vehicleApproachItemDisplayName(itemId)
    }
    is ReadoutItemKey.LmuWindows.Flag -> {
        flagItemDisplayName(itemId)
    }
    is ReadoutItemKey.LmuWindows.VehicleDamage -> {
        vehicleDamageItemDisplayName(itemId)
    }
    is ReadoutItemKey.LmuWindows.TyreTemperature -> {
        tyreTemperatureItemDisplayName(itemId)
    }
    is ReadoutItemKey.LmuWindows.PitTiming.Root -> {
        stringResource(Res.string.item_pit_timing)
    }
    is ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root -> {
        stringResource(Res.string.item_remaining_virtual_energy)
    }
    is ReadoutItemKey.LmuWindows.TyreWear.Root -> {
        stringResource(Res.string.item_tyre_wear)
    }
    is ReadoutItemKey.LmuWindows.MyBestLap.Root -> {
        stringResource(Res.string.item_my_best_lap)
    }
    is ReadoutItemKey.Gt7Ps5.MyBestLap.Root -> {
        stringResource(Res.string.item_my_best_lap)
    }
    is ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root -> {
        stringResource(Res.string.item_remaining_fuel_laps)
    }
    is ReadoutItemKey.Gt7Ps5.RemainingFuel.Root -> {
        stringResource(Res.string.item_remaining_fuel)
    }
    is ReadoutItemKey.AceWindows.Flag -> {
        aceFlagItemDisplayName(itemId)
    }
    is ReadoutItemKey.AceWindows.RemainingFuel.Root -> {
        stringResource(Res.string.item_remaining_fuel)
    }
}
