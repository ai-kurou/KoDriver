package kurou.kodriver.core.designsystem

import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.generated.resources.Res
import kurou.kodriver.core.designsystem.generated.resources.readout_item_ace_black_flag
import kurou.kodriver.core.designsystem.generated.resources.readout_item_ace_black_white_flag
import kurou.kodriver.core.designsystem.generated.resources.readout_item_ace_blue_flag
import kurou.kodriver.core.designsystem.generated.resources.readout_item_ace_checkered_flag
import kurou.kodriver.core.designsystem.generated.resources.readout_item_ace_green_flag
import kurou.kodriver.core.designsystem.generated.resources.readout_item_ace_orange_circle_flag
import kurou.kodriver.core.designsystem.generated.resources.readout_item_ace_red_flag
import kurou.kodriver.core.designsystem.generated.resources.readout_item_ace_red_yellow_stripes_flag
import kurou.kodriver.core.designsystem.generated.resources.readout_item_ace_white_flag
import kurou.kodriver.core.designsystem.generated.resources.readout_item_ace_yellow_flag
import kurou.kodriver.core.designsystem.generated.resources.readout_item_blue_flag
import kurou.kodriver.core.designsystem.generated.resources.readout_item_flag
import kurou.kodriver.core.designsystem.generated.resources.readout_item_full_course_yellow
import kurou.kodriver.core.designsystem.generated.resources.readout_item_my_best_lap
import kurou.kodriver.core.designsystem.generated.resources.readout_item_overheat
import kurou.kodriver.core.designsystem.generated.resources.readout_item_pit_timing
import kurou.kodriver.core.designsystem.generated.resources.readout_item_red_flag
import kurou.kodriver.core.designsystem.generated.resources.readout_item_remaining_fuel
import kurou.kodriver.core.designsystem.generated.resources.readout_item_remaining_fuel_laps
import kurou.kodriver.core.designsystem.generated.resources.readout_item_remaining_virtual_energy
import kurou.kodriver.core.designsystem.generated.resources.readout_item_sector_yellow_flag
import kurou.kodriver.core.designsystem.generated.resources.readout_item_tyre_low_warning
import kurou.kodriver.core.designsystem.generated.resources.readout_item_tyre_overheat_warning
import kurou.kodriver.core.designsystem.generated.resources.readout_item_tyre_temperature
import kurou.kodriver.core.designsystem.generated.resources.readout_item_tyre_wear
import kurou.kodriver.core.designsystem.generated.resources.readout_item_vehicle_approach
import kurou.kodriver.core.designsystem.generated.resources.readout_item_vehicle_approach_start_readout
import kurou.kodriver.core.designsystem.generated.resources.readout_item_vehicle_approach_sustained
import kurou.kodriver.core.designsystem.generated.resources.readout_item_vehicle_damage
import org.jetbrains.compose.resources.stringResource

private const val LMU_WINDOWS_VEHICLE_APPROACH_ID = "lmu_windows_vehicle_approach"
private const val LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_ID = "lmu_windows_vehicle_approach_sustained"
private const val LMU_WINDOWS_VEHICLE_APPROACH_START_READOUT_ID = "lmu_windows_vehicle_approach_start_readout"
private const val LMU_WINDOWS_MY_BEST_LAP_ID = "lmu_windows_my_best_lap"
private const val LMU_WINDOWS_FLAG_ID = "lmu_windows_flag"
private const val LMU_WINDOWS_BLUE_FLAG_ID = "lmu_windows_blue_flag"
private const val LMU_WINDOWS_SECTOR_YELLOW_FLAG_ID = "lmu_windows_sector_yellow_flag"
private const val LMU_WINDOWS_FULL_COURSE_YELLOW_ID = "lmu_windows_full_course_yellow"
private const val LMU_WINDOWS_RED_FLAG_ID = "lmu_windows_red_flag"
private const val LMU_WINDOWS_VEHICLE_DAMAGE_ID = "lmu_windows_vehicle_damage"
private const val LMU_WINDOWS_OVERHEAT_ID = "lmu_windows_overheat"
private const val LMU_WINDOWS_TYRE_TEMPERATURE_ID = "lmu_windows_tyre_temperature"
private const val LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_WARNING_ID = "lmu_windows_tyre_temperature_overheat_warning"
private const val LMU_WINDOWS_TYRE_TEMPERATURE_LOW_WARNING_ID = "lmu_windows_tyre_temperature_low_warning"
private const val LMU_WINDOWS_PIT_TIMING_ID = "lmu_windows_pit_timing"
private const val LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_ID = "lmu_windows_remaining_virtual_energy"
private const val LMU_WINDOWS_TYRE_WEAR_ID = "lmu_windows_tyre_wear"
private const val GT7_PS5_MY_BEST_LAP_ID = "gt7_ps5_my_best_lap"
private const val GT7_PS5_REMAINING_FUEL_LAPS_ID = "gt7_ps5_remaining_fuel_laps"
private const val GT7_PS5_REMAINING_FUEL_ID = "gt7_ps5_remaining_fuel"
private const val ACE_WINDOWS_FLAG_ID = "ace_windows_flag"
private const val ACE_WINDOWS_WHITE_FLAG_ID = "ace_windows_white_flag"
private const val ACE_WINDOWS_GREEN_FLAG_ID = "ace_windows_green_flag"
private const val ACE_WINDOWS_RED_FLAG_ID = "ace_windows_red_flag"
private const val ACE_WINDOWS_BLUE_FLAG_ID = "ace_windows_blue_flag"
private const val ACE_WINDOWS_YELLOW_FLAG_ID = "ace_windows_yellow_flag"
private const val ACE_WINDOWS_BLACK_FLAG_ID = "ace_windows_black_flag"
private const val ACE_WINDOWS_BLACK_WHITE_FLAG_ID = "ace_windows_black_white_flag"
private const val ACE_WINDOWS_CHECKERED_FLAG_ID = "ace_windows_checkered_flag"
private const val ACE_WINDOWS_ORANGE_CIRCLE_FLAG_ID = "ace_windows_orange_circle_flag"
private const val ACE_WINDOWS_RED_YELLOW_STRIPES_FLAG_ID = "ace_windows_red_yellow_stripes_flag"
private const val ACE_WINDOWS_REMAINING_FUEL_ID = "ace_windows_remaining_fuel"

@Composable
private fun vehicleApproachDisplayName(readoutItemId: String): String =
    when (readoutItemId) {
        LMU_WINDOWS_VEHICLE_APPROACH_ID -> stringResource(Res.string.readout_item_vehicle_approach)
        LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_ID -> stringResource(Res.string.readout_item_vehicle_approach_sustained)
        else -> stringResource(Res.string.readout_item_vehicle_approach_start_readout)
    }

@Composable
private fun lmuWindowsFlagDisplayName(readoutItemId: String): String =
    when (readoutItemId) {
        LMU_WINDOWS_FLAG_ID -> stringResource(Res.string.readout_item_flag)
        LMU_WINDOWS_BLUE_FLAG_ID -> stringResource(Res.string.readout_item_blue_flag)
        LMU_WINDOWS_SECTOR_YELLOW_FLAG_ID -> stringResource(Res.string.readout_item_sector_yellow_flag)
        LMU_WINDOWS_FULL_COURSE_YELLOW_ID -> stringResource(Res.string.readout_item_full_course_yellow)
        else -> stringResource(Res.string.readout_item_red_flag)
    }

@Composable
private fun aceWindowsFlagDisplayName(readoutItemId: String): String =
    when (readoutItemId) {
        ACE_WINDOWS_FLAG_ID -> stringResource(Res.string.readout_item_flag)
        ACE_WINDOWS_WHITE_FLAG_ID -> stringResource(Res.string.readout_item_ace_white_flag)
        ACE_WINDOWS_GREEN_FLAG_ID -> stringResource(Res.string.readout_item_ace_green_flag)
        ACE_WINDOWS_RED_FLAG_ID -> stringResource(Res.string.readout_item_ace_red_flag)
        ACE_WINDOWS_BLUE_FLAG_ID -> stringResource(Res.string.readout_item_ace_blue_flag)
        ACE_WINDOWS_YELLOW_FLAG_ID -> stringResource(Res.string.readout_item_ace_yellow_flag)
        ACE_WINDOWS_BLACK_FLAG_ID -> stringResource(Res.string.readout_item_ace_black_flag)
        ACE_WINDOWS_BLACK_WHITE_FLAG_ID -> stringResource(Res.string.readout_item_ace_black_white_flag)
        ACE_WINDOWS_CHECKERED_FLAG_ID -> stringResource(Res.string.readout_item_ace_checkered_flag)
        ACE_WINDOWS_ORANGE_CIRCLE_FLAG_ID -> stringResource(Res.string.readout_item_ace_orange_circle_flag)
        else -> stringResource(Res.string.readout_item_ace_red_yellow_stripes_flag)
    }

@Composable
private fun tyreTemperatureDisplayName(readoutItemId: String): String =
    when (readoutItemId) {
        LMU_WINDOWS_TYRE_TEMPERATURE_ID -> {
            stringResource(Res.string.readout_item_tyre_temperature)
        }

        LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_WARNING_ID -> {
            stringResource(
                Res.string.readout_item_tyre_overheat_warning,
            )
        }

        else -> {
            stringResource(Res.string.readout_item_tyre_low_warning)
        }
    }

/**
 * [readoutItemId] は `kurou.kodriver.domain.model.ReadoutItemKey.value` の値と一致させる必要がある。
 * このモジュールは core:domain に依存しないため、型ではなく文字列IDを引数にとる。
 */
@Composable
fun readoutItemDisplayName(readoutItemId: String): String =
    when (readoutItemId) {
        LMU_WINDOWS_VEHICLE_APPROACH_ID,
        LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_ID,
        LMU_WINDOWS_VEHICLE_APPROACH_START_READOUT_ID,
        -> {
            vehicleApproachDisplayName(readoutItemId)
        }

        LMU_WINDOWS_MY_BEST_LAP_ID, GT7_PS5_MY_BEST_LAP_ID -> {
            stringResource(Res.string.readout_item_my_best_lap)
        }

        LMU_WINDOWS_FLAG_ID,
        LMU_WINDOWS_BLUE_FLAG_ID,
        LMU_WINDOWS_SECTOR_YELLOW_FLAG_ID,
        LMU_WINDOWS_FULL_COURSE_YELLOW_ID,
        LMU_WINDOWS_RED_FLAG_ID,
        -> {
            lmuWindowsFlagDisplayName(readoutItemId)
        }

        ACE_WINDOWS_FLAG_ID,
        ACE_WINDOWS_WHITE_FLAG_ID,
        ACE_WINDOWS_GREEN_FLAG_ID,
        ACE_WINDOWS_RED_FLAG_ID,
        ACE_WINDOWS_BLUE_FLAG_ID,
        ACE_WINDOWS_YELLOW_FLAG_ID,
        ACE_WINDOWS_BLACK_FLAG_ID,
        ACE_WINDOWS_BLACK_WHITE_FLAG_ID,
        ACE_WINDOWS_CHECKERED_FLAG_ID,
        ACE_WINDOWS_ORANGE_CIRCLE_FLAG_ID,
        ACE_WINDOWS_RED_YELLOW_STRIPES_FLAG_ID,
        -> {
            aceWindowsFlagDisplayName(readoutItemId)
        }

        LMU_WINDOWS_VEHICLE_DAMAGE_ID -> {
            stringResource(Res.string.readout_item_vehicle_damage)
        }

        LMU_WINDOWS_OVERHEAT_ID -> {
            stringResource(Res.string.readout_item_overheat)
        }

        LMU_WINDOWS_TYRE_TEMPERATURE_ID,
        LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_WARNING_ID,
        LMU_WINDOWS_TYRE_TEMPERATURE_LOW_WARNING_ID,
        -> {
            tyreTemperatureDisplayName(readoutItemId)
        }

        LMU_WINDOWS_PIT_TIMING_ID -> {
            stringResource(Res.string.readout_item_pit_timing)
        }

        LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_ID -> {
            stringResource(Res.string.readout_item_remaining_virtual_energy)
        }

        LMU_WINDOWS_TYRE_WEAR_ID -> {
            stringResource(Res.string.readout_item_tyre_wear)
        }

        GT7_PS5_REMAINING_FUEL_LAPS_ID -> {
            stringResource(Res.string.readout_item_remaining_fuel_laps)
        }

        GT7_PS5_REMAINING_FUEL_ID, ACE_WINDOWS_REMAINING_FUEL_ID -> {
            stringResource(
                Res.string.readout_item_remaining_fuel,
            )
        }

        else -> {
            error("未対応のreadoutItemId: $readoutItemId")
        }
    }
