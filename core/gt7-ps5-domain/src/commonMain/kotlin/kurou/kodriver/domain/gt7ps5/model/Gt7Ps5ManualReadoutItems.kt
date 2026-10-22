package kurou.kodriver.domain.gt7ps5.model

import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutItemDefinition
import kurou.kodriver.domain.model.manualReadoutItemDefinitionMapOf
import kurou.kodriver.domain.model.manualReadoutItemDefinitionOf

val gt7Ps5ManualReadoutItemDefinitions: Map<String, ManualReadoutItemDefinition> =
    manualReadoutItemDefinitionMapOf(
        manualReadoutItemDefinitionOf(
            Gt7Ps5ReadoutItemKey.RemainingFuel.Root,
            "残り燃料",
            GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT,
        ),
        manualReadoutItemDefinitionOf(
            Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
            "残り燃料の周回数",
            GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT,
        ),
        manualReadoutItemDefinitionOf(
            Gt7Ps5ReadoutItemKey.TyreTemperature.Root,
            "タイヤ温度",
            GT7_PS5_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT,
        ),
        manualReadoutItemDefinitionOf(
            Gt7Ps5ReadoutItemKey.MyBestLap.Root,
            "自己ベストラップ",
            GT7_PS5_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT,
        ),
    )

fun gt7Ps5ManualReadoutItemKeys(): List<String> = gt7Ps5ManualReadoutItemDefinitions.keys.toList()

fun gt7Ps5ManualReadoutItemName(itemKey: String): String = gt7Ps5ManualReadoutItemDefinitions[itemKey]?.name.orEmpty()

fun gt7Ps5ManualReadoutItemTextDefault(itemKey: String): String =
    gt7Ps5ManualReadoutItemDefinitions[itemKey]?.textDefault.orEmpty()
