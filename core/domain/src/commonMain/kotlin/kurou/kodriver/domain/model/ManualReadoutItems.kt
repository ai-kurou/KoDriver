package kurou.kodriver.domain.model

import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT
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

/** 手動読み上げ項目をシミュレーターとキーごとに定義する。 */
data class ManualReadoutItemDefinition(
    val key: String,
    val name: String,
    val textDefault: String,
)

private fun manualReadoutItemDefinition(
    key: ReadoutItemKey,
    name: String,
    textDefault: String,
) = ManualReadoutItemDefinition(key.value, name, textDefault)

private fun manualReadoutItemDefinitions(vararg definitions: ManualReadoutItemDefinition) =
    definitions.associateBy { it.key }

/** シミュレーターごとの手動読み上げ項目定義。 */
val manualReadoutItemDefinitions: Map<String, Map<String, ManualReadoutItemDefinition>> =
    linkedMapOf(
        Simulator.LmuWindows.id to
            manualReadoutItemDefinitions(
                manualReadoutItemDefinition(
                    LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
                    "バーチャルエネルギー残量",
                    LMU_WINDOWS_MANUAL_READOUT_REMAINING_VIRTUAL_ENERGY_TEXT_DEFAULT,
                ),
                manualReadoutItemDefinition(
                    LmuWindowsReadoutItemKey.TyreWear.Root,
                    "タイヤ摩耗",
                    LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_TEXT_DEFAULT,
                ),
                manualReadoutItemDefinition(
                    LmuWindowsReadoutItemKey.BrakeWear.Root,
                    "ブレーキ残量",
                    LMU_WINDOWS_MANUAL_READOUT_BRAKE_WEAR_TEXT_DEFAULT,
                ),
                manualReadoutItemDefinition(
                    LmuWindowsReadoutItemKey.BrakeTemperature.Root,
                    "ブレーキ温度",
                    LMU_WINDOWS_MANUAL_READOUT_BRAKE_TEMPERATURE_TEXT_DEFAULT,
                ),
                manualReadoutItemDefinition(
                    LmuWindowsReadoutItemKey.TyreTemperature.Root,
                    "タイヤ温度",
                    LMU_WINDOWS_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT,
                ),
                manualReadoutItemDefinition(
                    LmuWindowsReadoutItemKey.MyBestLap.Root,
                    "自己ベストラップ",
                    LMU_WINDOWS_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT,
                ),
                manualReadoutItemDefinition(
                    LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy,
                    "エネルギーの残り周回数",
                    LMU_WINDOWS_MANUAL_READOUT_VIRTUAL_ENERGY_LAPS_TEXT_DEFAULT,
                ),
                manualReadoutItemDefinition(
                    LmuWindowsReadoutItemKey.PitTiming.TyreWear,
                    "タイヤ摩耗の残り周回数",
                    LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_LAPS_TEXT_DEFAULT,
                ),
            ),
        Simulator.AceWindows.id to
            manualReadoutItemDefinitions(
                manualReadoutItemDefinition(
                    AceWindowsReadoutItemKey.RemainingFuel.Root,
                    "残り燃料",
                    ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT,
                ),
                manualReadoutItemDefinition(
                    AceWindowsReadoutItemKey.RemainingFuelLaps.Root,
                    "残り燃料の周回数",
                    ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT,
                ),
                manualReadoutItemDefinition(
                    AceWindowsReadoutItemKey.MyBestLap.Root,
                    "自己ベストラップ",
                    ACE_WINDOWS_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT,
                ),
                manualReadoutItemDefinition(
                    AceWindowsReadoutItemKey.TyreTemperature.Root,
                    "タイヤ温度",
                    ACE_WINDOWS_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT,
                ),
            ),
        Simulator.Gt7Ps5.id to
            manualReadoutItemDefinitions(
                manualReadoutItemDefinition(
                    Gt7Ps5ReadoutItemKey.RemainingFuel.Root,
                    "残り燃料",
                    GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT,
                ),
                manualReadoutItemDefinition(
                    Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
                    "残り燃料の周回数",
                    GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT,
                ),
                manualReadoutItemDefinition(
                    Gt7Ps5ReadoutItemKey.TyreTemperature.Root,
                    "タイヤ温度",
                    GT7_PS5_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT,
                ),
                manualReadoutItemDefinition(
                    Gt7Ps5ReadoutItemKey.MyBestLap.Root,
                    "自己ベストラップ",
                    GT7_PS5_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT,
                ),
            ),
    )

/** simulatorId から利用可能なキーを順序を保って返す。 */
fun manualReadoutItemKeys(simulatorId: String): List<String> =
    manualReadoutItemDefinitions[simulatorId]?.keys?.toList().orEmpty()

fun manualReadoutItemDefinition(
    simulatorId: String,
    itemKey: String,
): ManualReadoutItemDefinition? = manualReadoutItemDefinitions[simulatorId]?.get(itemKey)

/** 手動読み上げ項目の既定文言。未対応の項目は空文字を返す。 */
fun manualReadoutItemTextDefault(
    simulatorId: String,
    itemKey: String,
): String = manualReadoutItemDefinition(simulatorId, itemKey)?.textDefault.orEmpty()

/** 手動読み上げ項目の名前。未対応の項目は空文字を返す。 */
fun manualReadoutItemName(
    simulatorId: String,
    itemKey: String,
): String = manualReadoutItemDefinition(simulatorId, itemKey)?.name.orEmpty()

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
