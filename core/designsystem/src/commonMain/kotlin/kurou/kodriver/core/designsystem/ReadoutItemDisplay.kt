package kurou.kodriver.core.designsystem

import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.generated.resources.Res
import kurou.kodriver.core.designsystem.generated.resources.readout_item_ace_black_flag
import kurou.kodriver.core.designsystem.generated.resources.readout_item_ace_black_white_flag
import kurou.kodriver.core.designsystem.generated.resources.readout_item_ace_blue_flag
import kurou.kodriver.core.designsystem.generated.resources.readout_item_ace_checkered_flag
import kurou.kodriver.core.designsystem.generated.resources.readout_item_ace_green_flag
import kurou.kodriver.core.designsystem.generated.resources.readout_item_ace_orange_circle_flag
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

@Composable
private fun aceFlagDisplayName(readoutItemKeyValue: String): String? =
    when (readoutItemKeyValue) {
        "ace_windows_white_flag" -> stringResource(Res.string.readout_item_ace_white_flag)
        "ace_windows_green_flag" -> stringResource(Res.string.readout_item_ace_green_flag)
        "ace_windows_blue_flag" -> stringResource(Res.string.readout_item_ace_blue_flag)
        "ace_windows_yellow_flag" -> stringResource(Res.string.readout_item_ace_yellow_flag)
        "ace_windows_black_flag" -> stringResource(Res.string.readout_item_ace_black_flag)
        "ace_windows_black_white_flag" -> stringResource(Res.string.readout_item_ace_black_white_flag)
        "ace_windows_checkered_flag" -> stringResource(Res.string.readout_item_ace_checkered_flag)
        "ace_windows_orange_circle_flag" -> stringResource(Res.string.readout_item_ace_orange_circle_flag)
        "ace_windows_red_yellow_stripes_flag" -> stringResource(Res.string.readout_item_ace_red_yellow_stripes_flag)
        else -> null
    }

@Composable
private fun flagDisplayName(readoutItemKeyValue: String): String? =
    when (readoutItemKeyValue) {
        "lmu_windows_flag", "ace_windows_flag" -> stringResource(Res.string.readout_item_flag)
        "lmu_windows_blue_flag" -> stringResource(Res.string.readout_item_blue_flag)
        "lmu_windows_sector_yellow_flag" -> stringResource(Res.string.readout_item_sector_yellow_flag)
        "lmu_windows_full_course_yellow" -> stringResource(Res.string.readout_item_full_course_yellow)
        "lmu_windows_red_flag", "ace_windows_red_flag" -> stringResource(Res.string.readout_item_red_flag)
        else -> aceFlagDisplayName(readoutItemKeyValue)
    }

@Composable
private fun vehicleApproachDisplayName(readoutItemKeyValue: String): String? =
    when (readoutItemKeyValue) {
        "lmu_windows_vehicle_approach" -> {
            stringResource(Res.string.readout_item_vehicle_approach)
        }

        "lmu_windows_vehicle_approach_sustained" -> {
            stringResource(Res.string.readout_item_vehicle_approach_sustained)
        }

        "lmu_windows_vehicle_approach_start_readout" -> {
            stringResource(Res.string.readout_item_vehicle_approach_start_readout)
        }

        else -> {
            null
        }
    }

@Composable
private fun tyreTemperatureDisplayName(readoutItemKeyValue: String): String? =
    when (readoutItemKeyValue) {
        "lmu_windows_tyre_temperature" -> stringResource(Res.string.readout_item_tyre_temperature)
        "lmu_windows_tyre_temperature_overheat_warning" -> stringResource(Res.string.readout_item_tyre_overheat_warning)
        "lmu_windows_tyre_temperature_low_warning" -> stringResource(Res.string.readout_item_tyre_low_warning)
        else -> null
    }

@Composable
private fun vehicleDamageDisplayName(readoutItemKeyValue: String): String? =
    when (readoutItemKeyValue) {
        "lmu_windows_vehicle_damage" -> stringResource(Res.string.readout_item_vehicle_damage)
        "lmu_windows_overheat" -> stringResource(Res.string.readout_item_overheat)
        else -> null
    }

/** LMU にのみ存在し、サブ項目を持たない単独の設定項目（ピットタイミング・バーチャルエナジー残量・タイヤ摩耗）。 */
@Composable
private fun lmuStandaloneDisplayName(readoutItemKeyValue: String): String? =
    when (readoutItemKeyValue) {
        "lmu_windows_pit_timing" -> stringResource(Res.string.readout_item_pit_timing)
        "lmu_windows_remaining_virtual_energy" -> stringResource(Res.string.readout_item_remaining_virtual_energy)
        "lmu_windows_tyre_wear" -> stringResource(Res.string.readout_item_tyre_wear)
        else -> null
    }

/** LMU・GT7・ACE をまたいで存在する自己ベストラップ・燃料関連の項目。 */
@Composable
private fun bestLapAndFuelDisplayName(readoutItemKeyValue: String): String? =
    when (readoutItemKeyValue) {
        "lmu_windows_my_best_lap", "gt7_ps5_my_best_lap" -> stringResource(Res.string.readout_item_my_best_lap)
        "gt7_ps5_remaining_fuel_laps" -> stringResource(Res.string.readout_item_remaining_fuel_laps)
        "gt7_ps5_remaining_fuel", "ace_windows_remaining_fuel" -> stringResource(Res.string.readout_item_remaining_fuel)
        else -> null
    }

/**
 * [readoutItemKeyValue] は `kurou.kodriver.domain.model.ReadoutItemKey.value` の値と一致させる必要がある。
 * このモジュールは core:domain に依存しないため、型ではなく文字列IDを引数にとる。
 */
@Composable
fun readoutItemDisplayName(readoutItemKeyValue: String): String =
    flagDisplayName(readoutItemKeyValue)
        ?: vehicleApproachDisplayName(readoutItemKeyValue)
        ?: vehicleDamageDisplayName(readoutItemKeyValue)
        ?: tyreTemperatureDisplayName(readoutItemKeyValue)
        ?: lmuStandaloneDisplayName(readoutItemKeyValue)
        ?: bestLapAndFuelDisplayName(readoutItemKeyValue)
        ?: error("未対応のreadoutItemKeyValue: $readoutItemKeyValue")
