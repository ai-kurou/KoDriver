package kurou.kodriver.feature.telemetryloglist

import androidx.compose.runtime.Composable
import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.feature.telemetryloglist.generated.resources.Res
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_ace_black_flag
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_ace_black_white_flag
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_ace_blue_flag
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_ace_checkered_flag
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_ace_green_flag
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_ace_orange_circle_flag
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_ace_red_flag
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_ace_red_yellow_stripes_flag
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_ace_white_flag
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_ace_yellow_flag
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_blue_flag
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_flag
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_full_course_yellow
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_my_best_lap
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_overheat
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_pit_timing
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_red_flag
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_remaining_fuel
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_remaining_fuel_laps
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_remaining_virtual_energy
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_sector_yellow_flag
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_tyre_low_warning
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_tyre_overheat_warning
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_tyre_temperature
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_tyre_wear
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_vehicle_approach
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_vehicle_approach_start_readout
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_vehicle_approach_sustained
import kurou.kodriver.feature.telemetryloglist.generated.resources.readout_item_vehicle_damage
import org.jetbrains.compose.resources.stringResource

@Composable
private fun flagDisplayName(flag: ReadoutItemKey.LmuWindows.Flag): String =
    when (flag) {
        is ReadoutItemKey.LmuWindows.Flag.Root -> stringResource(Res.string.readout_item_flag)
        is ReadoutItemKey.LmuWindows.Flag.BlueFlag -> stringResource(Res.string.readout_item_blue_flag)
        is ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag -> stringResource(Res.string.readout_item_sector_yellow_flag)
        is ReadoutItemKey.LmuWindows.Flag.FullCourseYellow -> stringResource(Res.string.readout_item_full_course_yellow)
        is ReadoutItemKey.LmuWindows.Flag.RedFlag -> stringResource(Res.string.readout_item_red_flag)
    }

@Composable
private fun aceFlagDisplayName(flag: ReadoutItemKey.AceWindows.Flag): String =
    when (flag) {
        is ReadoutItemKey.AceWindows.Flag.Root -> {
            stringResource(Res.string.readout_item_flag)
        }

        is ReadoutItemKey.AceWindows.Flag.WhiteFlag -> {
            stringResource(Res.string.readout_item_ace_white_flag)
        }

        is ReadoutItemKey.AceWindows.Flag.GreenFlag -> {
            stringResource(Res.string.readout_item_ace_green_flag)
        }

        is ReadoutItemKey.AceWindows.Flag.RedFlag -> {
            stringResource(Res.string.readout_item_ace_red_flag)
        }

        is ReadoutItemKey.AceWindows.Flag.BlueFlag -> {
            stringResource(Res.string.readout_item_ace_blue_flag)
        }

        is ReadoutItemKey.AceWindows.Flag.YellowFlag -> {
            stringResource(Res.string.readout_item_ace_yellow_flag)
        }

        is ReadoutItemKey.AceWindows.Flag.BlackFlag -> {
            stringResource(Res.string.readout_item_ace_black_flag)
        }

        is ReadoutItemKey.AceWindows.Flag.BlackWhiteFlag -> {
            stringResource(Res.string.readout_item_ace_black_white_flag)
        }

        is ReadoutItemKey.AceWindows.Flag.CheckeredFlag -> {
            stringResource(Res.string.readout_item_ace_checkered_flag)
        }

        is ReadoutItemKey.AceWindows.Flag.OrangeCircleFlag -> {
            stringResource(Res.string.readout_item_ace_orange_circle_flag)
        }

        is ReadoutItemKey.AceWindows.Flag.RedYellowStripesFlag -> {
            stringResource(Res.string.readout_item_ace_red_yellow_stripes_flag)
        }
    }

@Composable
private fun vehicleApproachDisplayName(vehicleApproach: ReadoutItemKey.LmuWindows.VehicleApproach): String =
    when (vehicleApproach) {
        is ReadoutItemKey.LmuWindows.VehicleApproach.Root -> {
            stringResource(Res.string.readout_item_vehicle_approach)
        }

        is ReadoutItemKey.LmuWindows.VehicleApproach.Sustained -> {
            stringResource(Res.string.readout_item_vehicle_approach_sustained)
        }

        is ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout -> {
            stringResource(Res.string.readout_item_vehicle_approach_start_readout)
        }
    }

@Composable
private fun tyreTemperatureDisplayName(tyreTemperature: ReadoutItemKey.LmuWindows.TyreTemperature): String =
    when (tyreTemperature) {
        is ReadoutItemKey.LmuWindows.TyreTemperature.Root -> {
            stringResource(Res.string.readout_item_tyre_temperature)
        }

        is ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning -> {
            stringResource(Res.string.readout_item_tyre_overheat_warning)
        }

        is ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning -> {
            stringResource(Res.string.readout_item_tyre_low_warning)
        }
    }

@Composable
private fun vehicleDamageDisplayName(vehicleDamage: ReadoutItemKey.LmuWindows.VehicleDamage): String =
    when (vehicleDamage) {
        is ReadoutItemKey.LmuWindows.VehicleDamage.Root -> stringResource(Res.string.readout_item_vehicle_damage)
        is ReadoutItemKey.LmuWindows.VehicleDamage.Overheat -> stringResource(Res.string.readout_item_overheat)
    }

@Composable
internal fun readoutItemDisplayName(readoutItemKey: ReadoutItemKey): String =
    when (readoutItemKey) {
        is ReadoutItemKey.LmuWindows.VehicleApproach -> {
            vehicleApproachDisplayName(readoutItemKey)
        }

        is ReadoutItemKey.LmuWindows.Flag -> {
            flagDisplayName(readoutItemKey)
        }

        is ReadoutItemKey.LmuWindows.VehicleDamage -> {
            vehicleDamageDisplayName(readoutItemKey)
        }

        is ReadoutItemKey.LmuWindows.TyreTemperature -> {
            tyreTemperatureDisplayName(readoutItemKey)
        }

        is ReadoutItemKey.LmuWindows.PitTiming.Root -> {
            stringResource(Res.string.readout_item_pit_timing)
        }

        is ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root -> {
            stringResource(Res.string.readout_item_remaining_virtual_energy)
        }

        is ReadoutItemKey.LmuWindows.TyreWear.Root -> {
            stringResource(Res.string.readout_item_tyre_wear)
        }

        is ReadoutItemKey.LmuWindows.MyBestLap.Root -> {
            stringResource(Res.string.readout_item_my_best_lap)
        }

        is ReadoutItemKey.Gt7Ps5.MyBestLap.Root -> {
            stringResource(Res.string.readout_item_my_best_lap)
        }

        is ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root -> {
            stringResource(Res.string.readout_item_remaining_fuel_laps)
        }

        is ReadoutItemKey.Gt7Ps5.RemainingFuel.Root -> {
            stringResource(Res.string.readout_item_remaining_fuel)
        }

        is ReadoutItemKey.AceWindows.Flag -> {
            aceFlagDisplayName(readoutItemKey)
        }

        is ReadoutItemKey.AceWindows.RemainingFuel.Root -> {
            stringResource(Res.string.readout_item_remaining_fuel)
        }
    }
