package kurou.kodriver.domain.gt7ps5.model

import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutItemCategory
import kurou.kodriver.domain.model.ManualReadoutItemDefinition
import kurou.kodriver.domain.model.manualReadoutItemDefinitionMapOf
import kurou.kodriver.domain.model.manualReadoutItemDefinitionOf

private const val PREVIEW_ENERGY_PERCENT = 50
private const val PREVIEW_REMAINING_FUEL_LAPS = 3
private const val PREVIEW_BEST_LAP_TIME_MS = 83_456
private const val PREVIEW_TYRE_TEMPERATURE_CELSIUS = 90

val gt7Ps5ManualReadoutItemDefinitions: Map<String, ManualReadoutItemDefinition> =
    manualReadoutItemDefinitionMapOf(
        manualReadoutItemDefinitionOf(
            Gt7Ps5ReadoutItemKey.RemainingFuel.Root,
            ManualReadoutItemCategory.FUEL,
            "残り燃料",
            GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT,
            GT7_PS5_REMAINING_FUEL_PERCENT_PLACEHOLDER,
            ::findUnknownGt7Ps5RemainingFuelReadoutPlaceholders,
            { formatGt7Ps5RemainingFuelReadoutText(it, PREVIEW_ENERGY_PERCENT) },
        ),
        manualReadoutItemDefinitionOf(
            Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
            ManualReadoutItemCategory.FUEL,
            "残り燃料の周回数",
            GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT,
            GT7_PS5_REMAINING_FUEL_LAPS_PLACEHOLDER,
            ::findUnknownGt7Ps5RemainingFuelLapsReadoutPlaceholders,
            { formatGt7Ps5RemainingFuelLapsReadoutText(it, PREVIEW_REMAINING_FUEL_LAPS) },
        ),
        manualReadoutItemDefinitionOf(
            Gt7Ps5ReadoutItemKey.TyreTemperature.Root,
            ManualReadoutItemCategory.TYRE,
            "タイヤ温度",
            GT7_PS5_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT,
            GT7_PS5_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER,
            ::findUnknownGt7Ps5TyreTemperatureReadoutPlaceholders,
            { formatGt7Ps5TyreTemperatureReadoutText(it, PREVIEW_TYRE_TEMPERATURE_CELSIUS) },
        ),
        manualReadoutItemDefinitionOf(
            Gt7Ps5ReadoutItemKey.MyBestLap.Root,
            ManualReadoutItemCategory.LAP,
            "自己ベストラップ",
            GT7_PS5_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT,
            GT7_PS5_MY_BEST_LAP_LAPTIME_PLACEHOLDER,
            ::findUnknownGt7Ps5MyBestLapReadoutPlaceholders,
            { formatGt7Ps5MyBestLapReadoutText(it, PREVIEW_BEST_LAP_TIME_MS) },
        ),
    )

fun gt7Ps5ManualReadoutItemKeys(): List<String> = gt7Ps5ManualReadoutItemDefinitions.keys.toList()

fun gt7Ps5ManualReadoutItemName(itemKey: String): String = gt7Ps5ManualReadoutItemDefinitions[itemKey]?.name.orEmpty()

fun gt7Ps5ManualReadoutItemTextDefault(itemKey: String): String =
    gt7Ps5ManualReadoutItemDefinitions[itemKey]?.textDefault.orEmpty()
