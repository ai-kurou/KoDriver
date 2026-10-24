package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutItemDefinition
import kurou.kodriver.domain.model.manualReadoutItemDefinitionMapOf
import kurou.kodriver.domain.model.manualReadoutItemDefinitionOf

val lmuWindowsManualReadoutItemDefinitions: Map<String, ManualReadoutItemDefinition> =
    manualReadoutItemDefinitionMapOf(
        manualReadoutItemDefinitionOf(
            LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
            "バーチャルエネルギー残量",
            LMU_WINDOWS_MANUAL_READOUT_REMAINING_VIRTUAL_ENERGY_TEXT_DEFAULT,
        ),
        manualReadoutItemDefinitionOf(
            LmuWindowsReadoutItemKey.RemainingFuel.Root,
            "燃料残量",
            LMU_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT,
        ),
        manualReadoutItemDefinitionOf(
            LmuWindowsReadoutItemKey.TyreWear.Root,
            "タイヤ摩耗",
            LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_TEXT_DEFAULT,
        ),
        manualReadoutItemDefinitionOf(
            LmuWindowsReadoutItemKey.BrakeWear.Root,
            "ブレーキ残量",
            LMU_WINDOWS_MANUAL_READOUT_BRAKE_WEAR_TEXT_DEFAULT,
        ),
        manualReadoutItemDefinitionOf(
            LmuWindowsReadoutItemKey.BrakeTemperature.Root,
            "ブレーキ温度",
            LMU_WINDOWS_MANUAL_READOUT_BRAKE_TEMPERATURE_TEXT_DEFAULT,
        ),
        manualReadoutItemDefinitionOf(
            LmuWindowsReadoutItemKey.TyreTemperature.Root,
            "タイヤ温度",
            LMU_WINDOWS_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT,
        ),
        manualReadoutItemDefinitionOf(
            LmuWindowsReadoutItemKey.MyBestLap.Root,
            "自己ベストラップ",
            LMU_WINDOWS_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT,
        ),
        manualReadoutItemDefinitionOf(
            LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy,
            "エネルギーの残り周回数",
            LMU_WINDOWS_MANUAL_READOUT_VIRTUAL_ENERGY_LAPS_TEXT_DEFAULT,
        ),
        manualReadoutItemDefinitionOf(
            LmuWindowsReadoutItemKey.PitTiming.TyreWear,
            "タイヤ摩耗の残り周回数",
            LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_LAPS_TEXT_DEFAULT,
        ),
    )

fun lmuWindowsManualReadoutItemKeys(): List<String> = lmuWindowsManualReadoutItemDefinitions.keys.toList()

fun lmuWindowsManualReadoutItemName(itemKey: String): String =
    lmuWindowsManualReadoutItemDefinitions[itemKey]?.name.orEmpty()

fun lmuWindowsManualReadoutItemTextDefault(itemKey: String): String =
    lmuWindowsManualReadoutItemDefinitions[itemKey]?.textDefault.orEmpty()
