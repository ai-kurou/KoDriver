package kurou.kodriver.domain.acewindows.model

import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutItemDefinition
import kurou.kodriver.domain.model.manualReadoutItemDefinitionMapOf
import kurou.kodriver.domain.model.manualReadoutItemDefinitionOf

val aceWindowsManualReadoutItemDefinitions: Map<String, ManualReadoutItemDefinition> =
    manualReadoutItemDefinitionMapOf(
        manualReadoutItemDefinitionOf(
            AceWindowsReadoutItemKey.RemainingFuel.Root,
            "残り燃料",
            ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT,
        ),
        manualReadoutItemDefinitionOf(
            AceWindowsReadoutItemKey.RemainingFuelLaps.Root,
            "残り燃料の周回数",
            ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT,
        ),
        manualReadoutItemDefinitionOf(
            AceWindowsReadoutItemKey.MyBestLap.Root,
            "自己ベストラップ",
            ACE_WINDOWS_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT,
        ),
        manualReadoutItemDefinitionOf(
            AceWindowsReadoutItemKey.TyreTemperature.Root,
            "タイヤ温度",
            ACE_WINDOWS_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT,
        ),
    )

fun aceWindowsManualReadoutItemKeys(): List<String> = aceWindowsManualReadoutItemDefinitions.keys.toList()

fun aceWindowsManualReadoutItemName(itemKey: String): String =
    aceWindowsManualReadoutItemDefinitions[itemKey]?.name.orEmpty()

fun aceWindowsManualReadoutItemTextDefault(itemKey: String): String =
    aceWindowsManualReadoutItemDefinitions[itemKey]?.textDefault.orEmpty()
