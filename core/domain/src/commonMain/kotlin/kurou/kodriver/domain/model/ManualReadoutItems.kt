package kurou.kodriver.domain.model

import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MANUAL_READOUT_BRAKE_TEMPERATURE_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MANUAL_READOUT_BRAKE_WEAR_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MANUAL_READOUT_REMAINING_VIRTUAL_ENERGY_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_LAPS_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MANUAL_READOUT_VIRTUAL_ENERGY_LAPS_TEXT_DEFAULT

/** シミュレーターで実装済みの手動読み上げ対象項目。 */
fun manualReadoutItemKeys(simulatorId: String): List<String> =
    when (simulatorId) {
        Simulator.LmuWindows.id -> {
            listOf(
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value,
                LmuWindowsReadoutItemKey.TyreWear.Root.value,
                LmuWindowsReadoutItemKey.BrakeWear.Root.value,
                LmuWindowsReadoutItemKey.BrakeTemperature.Root.value,
                LmuWindowsReadoutItemKey.TyreTemperature.Root.value,
                LmuWindowsReadoutItemKey.MyBestLap.Root.value,
                LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value,
                LmuWindowsReadoutItemKey.PitTiming.TyreWear.value,
            )
        }

        Simulator.AceWindows.id -> {
            listOf(
                AceWindowsReadoutItemKey.RemainingFuel.Root.value,
                AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value,
            )
        }

        Simulator.Gt7Ps5.id -> {
            listOf(
                Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value,
                Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value,
                Gt7Ps5ReadoutItemKey.TyreTemperature.Root.value,
                Gt7Ps5ReadoutItemKey.MyBestLap.Root.value,
            )
        }

        else -> {
            emptyList()
        }
    }

/** 手動読み上げ項目の既定文言。未対応の項目は空文字を返す。 */
fun manualReadoutItemTextDefault(itemKey: String): String =
    when (itemKey) {
        LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value -> {
            LMU_WINDOWS_MANUAL_READOUT_VIRTUAL_ENERGY_LAPS_TEXT_DEFAULT
        }

        LmuWindowsReadoutItemKey.PitTiming.TyreWear.value -> {
            LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_LAPS_TEXT_DEFAULT
        }

        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value -> {
            LMU_WINDOWS_MANUAL_READOUT_REMAINING_VIRTUAL_ENERGY_TEXT_DEFAULT
        }

        LmuWindowsReadoutItemKey.TyreWear.Root.value -> {
            LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_TEXT_DEFAULT
        }

        LmuWindowsReadoutItemKey.BrakeWear.Root.value -> {
            LMU_WINDOWS_MANUAL_READOUT_BRAKE_WEAR_TEXT_DEFAULT
        }

        LmuWindowsReadoutItemKey.BrakeTemperature.Root.value -> {
            LMU_WINDOWS_MANUAL_READOUT_BRAKE_TEMPERATURE_TEXT_DEFAULT
        }

        LmuWindowsReadoutItemKey.TyreTemperature.Root.value -> {
            LMU_WINDOWS_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT
        }

        LmuWindowsReadoutItemKey.MyBestLap.Root.value -> {
            LMU_WINDOWS_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT
        }

        else -> {
            aceManualReadoutItemTextDefault(itemKey)
        }
    }

private fun aceManualReadoutItemTextDefault(itemKey: String): String =
    when (itemKey) {
        AceWindowsReadoutItemKey.RemainingFuel.Root.value -> {
            ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT
        }

        AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value -> {
            ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT
        }

        else -> {
            gt7ManualReadoutItemTextDefault(itemKey)
        }
    }

private fun gt7ManualReadoutItemTextDefault(itemKey: String): String =
    when (itemKey) {
        Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value -> {
            GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT
        }

        Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value -> {
            GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT
        }

        Gt7Ps5ReadoutItemKey.TyreTemperature.Root.value -> {
            GT7_PS5_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT
        }

        Gt7Ps5ReadoutItemKey.MyBestLap.Root.value -> {
            GT7_PS5_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT
        }

        else -> {
            ""
        }
    }

/** 手動読み上げ項目の名前。未対応の項目は空文字を返す。 */
fun manualReadoutItemName(itemKey: String): String =
    when (itemKey) {
        LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value -> "エネルギーの残り周回数"
        LmuWindowsReadoutItemKey.PitTiming.TyreWear.value -> "タイヤ摩耗の残り周回数"
        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value -> "バーチャルエネルギー残量"
        LmuWindowsReadoutItemKey.TyreWear.Root.value -> "タイヤ摩耗"
        LmuWindowsReadoutItemKey.BrakeWear.Root.value -> "ブレーキ残量"
        LmuWindowsReadoutItemKey.BrakeTemperature.Root.value -> "ブレーキ温度"
        LmuWindowsReadoutItemKey.TyreTemperature.Root.value -> "タイヤ温度"
        LmuWindowsReadoutItemKey.MyBestLap.Root.value -> "自己ベストラップ"
        else -> aceManualReadoutItemName(itemKey)
    }

private fun aceManualReadoutItemName(itemKey: String): String =
    when (itemKey) {
        AceWindowsReadoutItemKey.RemainingFuel.Root.value -> "残り燃料"
        AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value -> "残り燃料の周回数"
        else -> gt7ManualReadoutItemName(itemKey)
    }

private fun gt7ManualReadoutItemName(itemKey: String): String =
    when (itemKey) {
        Gt7Ps5ReadoutItemKey.TyreTemperature.Root.value -> "タイヤ温度"
        Gt7Ps5ReadoutItemKey.MyBestLap.Root.value -> "自己ベストラップ"
        Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value -> "残り燃料"
        Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value -> "残り燃料の周回数"
        else -> ""
    }

/** 鮮度判定済みの手動読み上げ項目。残存率は0〜1、温度は℃、ラップタイムはms、残り周回数は整数で、未受信・鮮度切れは null。 */
data class LmuWindowsManualReadoutItem(
    val key: String,
    val textTemplate: String,
    val ratio: Double?,
    val celsius: Float? = null,
    val lapTimeMs: Long? = null,
    val remainingLaps: Int? = null,
)

/** 摂氏温度を使う手動読み上げ項目かを判定する。 */
fun isLmuWindowsManualReadoutTemperatureItem(itemKey: String): Boolean =
    itemKey == LmuWindowsReadoutItemKey.BrakeTemperature.Root.value ||
        itemKey == LmuWindowsReadoutItemKey.TyreTemperature.Root.value

/** 鮮度判定済みの GT7 手動読み上げ項目。残り燃料は％、残り燃料の周回数は周、タイヤ温度は℃、自己ベストラップはms。未受信・鮮度切れは null。 */
data class Gt7Ps5ManualReadoutItem(
    val key: String,
    val textTemplate: String,
    val value: Int?,
)

/** 鮮度判定済みの ACE 手動読み上げ項目。残り燃料は整数％、未受信・鮮度切れ・不正値は null。 */
data class AceWindowsManualReadoutItem(
    val key: String,
    val textTemplate: String,
    val value: Int?,
)
