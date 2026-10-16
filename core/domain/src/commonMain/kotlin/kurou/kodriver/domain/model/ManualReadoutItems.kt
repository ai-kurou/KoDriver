package kurou.kodriver.domain.model

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
            )
        }

        Simulator.Gt7Ps5.id -> {
            listOf(
                Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value,
                Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value,
            )
        }

        else -> {
            emptyList()
        }
    }

/** 手動読み上げ項目の既定文言。未対応の項目は空文字を返す。 */
fun manualReadoutItemTextDefault(itemKey: String): String =
    when (itemKey) {
        Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value -> {
            GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT
        }

        Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value -> {
            GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT
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

        else -> {
            ""
        }
    }

/** 手動読み上げ項目の名前。未対応の項目は空文字を返す。 */
fun manualReadoutItemName(itemKey: String): String =
    when (itemKey) {
        Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value -> "残り燃料"
        Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value -> "残り燃料の周回数"
        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value -> "バーチャルエナジー残量"
        LmuWindowsReadoutItemKey.TyreWear.Root.value -> "タイヤ摩耗"
        LmuWindowsReadoutItemKey.BrakeWear.Root.value -> "ブレーキ残量"
        LmuWindowsReadoutItemKey.BrakeTemperature.Root.value -> "ブレーキ温度"
        LmuWindowsReadoutItemKey.TyreTemperature.Root.value -> "タイヤ温度"
        else -> ""
    }

/** 鮮度判定済みの手動読み上げ項目。残存率は0〜1、温度は℃で、未受信・鮮度切れは null。 */
data class LmuWindowsManualReadoutItem(
    val key: String,
    val textTemplate: String,
    val ratio: Double?,
    val celsius: Float? = null,
)

/** 摂氏温度を使う手動読み上げ項目かを判定する。 */
fun isLmuWindowsManualReadoutTemperatureItem(itemKey: String): Boolean =
    itemKey == LmuWindowsReadoutItemKey.BrakeTemperature.Root.value ||
        itemKey == LmuWindowsReadoutItemKey.TyreTemperature.Root.value

/** 鮮度判定済みの GT7 手動読み上げ項目。残り燃料は％、残り燃料の周回数は周。未受信・鮮度切れは null。 */
data class Gt7Ps5ManualReadoutItem(
    val key: String,
    val textTemplate: String,
    val value: Int?,
)
