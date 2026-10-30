package kurou.kodriver.domain.acewindows.model

import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutItemCategory
import kurou.kodriver.domain.model.ManualReadoutItemDefinition
import kurou.kodriver.domain.model.manualReadoutItemDefinitionMapOf
import kurou.kodriver.domain.model.manualReadoutItemDefinitionOf

private const val PREVIEW_ENERGY_PERCENT = 50
private const val PREVIEW_REMAINING_FUEL_LAPS = 5
private const val PREVIEW_BEST_LAP_TIME_MS = 83_456
private const val PREVIEW_TYRE_TEMPERATURE_CELSIUS = 90

val aceWindowsManualReadoutItemDefinitions: Map<String, ManualReadoutItemDefinition> =
    manualReadoutItemDefinitionMapOf(
        manualReadoutItemDefinitionOf(
            AceWindowsReadoutItemKey.RemainingFuel.Root,
            ManualReadoutItemCategory.FUEL,
            "残り燃料",
            ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT,
            ACE_WINDOWS_REMAINING_FUEL_PERCENT_PLACEHOLDER,
            ::findUnknownAceWindowsRemainingFuelReadoutPlaceholders,
            { formatAceWindowsRemainingFuelReadoutText(it, PREVIEW_ENERGY_PERCENT) },
        ),
        manualReadoutItemDefinitionOf(
            AceWindowsReadoutItemKey.RemainingFuelLaps.Root,
            ManualReadoutItemCategory.FUEL,
            "残り燃料の周回数",
            ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT,
            ACE_WINDOWS_REMAINING_FUEL_LAPS_PLACEHOLDER,
            ::findUnknownAceWindowsRemainingFuelLapsReadoutPlaceholders,
            { formatAceWindowsRemainingFuelLapsReadoutText(it, PREVIEW_REMAINING_FUEL_LAPS) },
        ),
        manualReadoutItemDefinitionOf(
            AceWindowsReadoutItemKey.MyBestLap.Root,
            ManualReadoutItemCategory.LAP,
            "自己ベストラップ",
            ACE_WINDOWS_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT,
            ACE_WINDOWS_MY_BEST_LAP_LAPTIME_PLACEHOLDER,
            ::findUnknownAceWindowsMyBestLapReadoutPlaceholders,
            { formatAceWindowsMyBestLapReadoutText(it, PREVIEW_BEST_LAP_TIME_MS) },
        ),
        manualReadoutItemDefinitionOf(
            AceWindowsReadoutItemKey.TyreTemperature.Root,
            ManualReadoutItemCategory.TYRE,
            "タイヤ温度",
            ACE_WINDOWS_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT,
            ACE_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER,
            ::findUnknownAceWindowsTyreTemperatureReadoutPlaceholders,
            { formatAceWindowsTyreTemperatureReadoutText(it, PREVIEW_TYRE_TEMPERATURE_CELSIUS) },
        ),
    )

fun aceWindowsManualReadoutItemKeys(): List<String> = aceWindowsManualReadoutItemDefinitions.keys.toList()

fun aceWindowsManualReadoutItemName(itemKey: String): String =
    aceWindowsManualReadoutItemDefinitions[itemKey]?.name.orEmpty()

fun aceWindowsManualReadoutItemTextDefault(itemKey: String): String =
    aceWindowsManualReadoutItemDefinitions[itemKey]?.textDefault.orEmpty()
