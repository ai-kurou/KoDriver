package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutItemCategory
import kurou.kodriver.domain.model.ManualReadoutItemDefinition
import kurou.kodriver.domain.model.manualReadoutItemDefinitionMapOf
import kurou.kodriver.domain.model.manualReadoutItemDefinitionOf

private const val PREVIEW_PIT_TIMING_LAPS = 3
private const val PREVIEW_CLASS_CAR_GAP_SECONDS = 1.5
private const val PREVIEW_BEST_LAP_TIME_MS = 83_456L
private const val PREVIEW_TYRE_TEMPERATURE_CELSIUS = 90
private const val PREVIEW_BRAKE_TEMPERATURE_CELSIUS = 500
private const val PREVIEW_BRAKE_WEAR_PERCENT = 50
private const val PREVIEW_TYRE_WEAR_PERCENT = 50
private const val PREVIEW_ENERGY_PERCENT = 50

val lmuWindowsManualReadoutItemDefinitions: Map<String, ManualReadoutItemDefinition> =
    manualReadoutItemDefinitionMapOf(
        manualReadoutItemDefinitionOf(
            LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
            ManualReadoutItemCategory.VIRTUAL_ENERGY,
            "バーチャルエネルギー残量",
            LMU_WINDOWS_MANUAL_READOUT_REMAINING_VIRTUAL_ENERGY_TEXT_DEFAULT,
            LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_PERCENT_PLACEHOLDER,
            ::findUnknownLmuWindowsRemainingVirtualEnergyReadoutPlaceholders,
            { formatLmuWindowsRemainingVirtualEnergyReadoutText(it, PREVIEW_ENERGY_PERCENT) },
        ),
        manualReadoutItemDefinitionOf(
            LmuWindowsReadoutItemKey.RemainingFuel.Root,
            ManualReadoutItemCategory.FUEL,
            "燃料残量",
            LMU_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT,
            LMU_WINDOWS_REMAINING_FUEL_PERCENT_PLACEHOLDER,
            ::findUnknownLmuWindowsRemainingFuelReadoutPlaceholders,
            { formatLmuWindowsRemainingFuelReadoutText(it, PREVIEW_ENERGY_PERCENT) },
        ),
        manualReadoutItemDefinitionOf(
            LmuWindowsReadoutItemKey.TyreWear.Root,
            ManualReadoutItemCategory.TYRE,
            "タイヤ摩耗",
            LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_TEXT_DEFAULT,
            LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_PERCENT_PLACEHOLDER,
            ::findUnknownLmuWindowsRemainingVirtualEnergyReadoutPlaceholders,
            { formatLmuWindowsTyreWearReadoutText(it, PREVIEW_TYRE_WEAR_PERCENT) },
        ),
        manualReadoutItemDefinitionOf(
            LmuWindowsReadoutItemKey.BrakeWear.Root,
            ManualReadoutItemCategory.BRAKE,
            "ブレーキ残量",
            LMU_WINDOWS_MANUAL_READOUT_BRAKE_WEAR_TEXT_DEFAULT,
            LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_PERCENT_PLACEHOLDER,
            ::findUnknownLmuWindowsRemainingVirtualEnergyReadoutPlaceholders,
            { formatLmuWindowsBrakeWearReadoutText(it, PREVIEW_BRAKE_WEAR_PERCENT) },
        ),
        manualReadoutItemDefinitionOf(
            LmuWindowsReadoutItemKey.BrakeTemperature.Root,
            ManualReadoutItemCategory.BRAKE,
            "ブレーキ温度",
            LMU_WINDOWS_MANUAL_READOUT_BRAKE_TEMPERATURE_TEXT_DEFAULT,
            LMU_WINDOWS_BRAKE_TEMPERATURE_CELSIUS_PLACEHOLDER,
            ::findUnknownLmuWindowsBrakeTemperatureReadoutPlaceholders,
            { formatLmuWindowsBrakeTemperatureReadoutText(it, PREVIEW_BRAKE_TEMPERATURE_CELSIUS) },
        ),
        manualReadoutItemDefinitionOf(
            LmuWindowsReadoutItemKey.TyreTemperature.Root,
            ManualReadoutItemCategory.TYRE,
            "タイヤ温度",
            LMU_WINDOWS_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT,
            LMU_WINDOWS_BRAKE_TEMPERATURE_CELSIUS_PLACEHOLDER,
            ::findUnknownLmuWindowsBrakeTemperatureReadoutPlaceholders,
            { formatLmuWindowsTyreTemperatureReadoutText(it, PREVIEW_TYRE_TEMPERATURE_CELSIUS) },
        ),
        manualReadoutItemDefinitionOf(
            LmuWindowsReadoutItemKey.MyBestLap.Root,
            ManualReadoutItemCategory.LAP,
            "自己ベストラップ",
            LMU_WINDOWS_MANUAL_READOUT_MY_BEST_LAP_TEXT_DEFAULT,
            LMU_WINDOWS_MY_BEST_LAP_LAPTIME_PLACEHOLDER,
            ::findUnknownLmuWindowsMyBestLapReadoutPlaceholders,
            { formatLmuWindowsMyBestLapReadoutText(it, PREVIEW_BEST_LAP_TIME_MS) },
        ),
        manualReadoutItemDefinitionOf(
            LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy,
            ManualReadoutItemCategory.VIRTUAL_ENERGY,
            "バーチャルエネルギーの残り周回数",
            LMU_WINDOWS_MANUAL_READOUT_VIRTUAL_ENERGY_LAPS_TEXT_DEFAULT,
            LMU_WINDOWS_PIT_TIMING_LAPS_PLACEHOLDER,
            ::findUnknownLmuWindowsPitTimingReadoutPlaceholders,
            { formatLmuWindowsPitTimingReadoutText(it, PREVIEW_PIT_TIMING_LAPS) },
        ),
        manualReadoutItemDefinitionOf(
            LmuWindowsReadoutItemKey.PitTiming.TyreWear,
            ManualReadoutItemCategory.TYRE,
            "タイヤ摩耗の残り周回数",
            LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_LAPS_TEXT_DEFAULT,
            LMU_WINDOWS_PIT_TIMING_LAPS_PLACEHOLDER,
            ::findUnknownLmuWindowsPitTimingReadoutPlaceholders,
            { formatLmuWindowsPitTimingReadoutText(it, PREVIEW_PIT_TIMING_LAPS) },
        ),
        manualReadoutItemDefinitionOf(
            LmuWindowsReadoutItemKey.RemainingFuel.Laps,
            ManualReadoutItemCategory.FUEL,
            "燃料の残り周回数",
            LMU_WINDOWS_MANUAL_READOUT_FUEL_LAPS_TEXT_DEFAULT,
            LMU_WINDOWS_PIT_TIMING_LAPS_PLACEHOLDER,
            ::findUnknownLmuWindowsPitTimingReadoutPlaceholders,
            { formatLmuWindowsPitTimingReadoutText(it, PREVIEW_PIT_TIMING_LAPS) },
        ),
        manualReadoutItemDefinitionOf(
            LmuWindowsReadoutItemKey.ClassCarGap.Ahead,
            ManualReadoutItemCategory.LAP,
            "クラス内前方車とのギャップ",
            LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_AHEAD_TEXT_DEFAULT,
            LMU_WINDOWS_CLASS_CAR_GAP_SECONDS_PLACEHOLDER,
            ::findUnknownLmuWindowsClassCarGapReadoutPlaceholders,
            { formatLmuWindowsClassCarGapReadoutText(it, PREVIEW_CLASS_CAR_GAP_SECONDS) },
        ),
        manualReadoutItemDefinitionOf(
            LmuWindowsReadoutItemKey.ClassCarGap.Behind,
            ManualReadoutItemCategory.LAP,
            "クラス内後方車とのギャップ",
            LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_BEHIND_TEXT_DEFAULT,
            LMU_WINDOWS_CLASS_CAR_GAP_SECONDS_PLACEHOLDER,
            ::findUnknownLmuWindowsClassCarGapReadoutPlaceholders,
            { formatLmuWindowsClassCarGapReadoutText(it, PREVIEW_CLASS_CAR_GAP_SECONDS) },
        ),
    )

fun lmuWindowsManualReadoutItemKeys(): List<String> = lmuWindowsManualReadoutItemDefinitions.keys.toList()

fun lmuWindowsManualReadoutItemName(itemKey: String): String =
    lmuWindowsManualReadoutItemDefinitions[itemKey]?.name.orEmpty()

fun lmuWindowsManualReadoutItemTextDefault(itemKey: String): String =
    lmuWindowsManualReadoutItemDefinitions[itemKey]?.textDefault.orEmpty()

/**
 * 先頭項目キーから手動読み上げの event キーを決める対応表。未登録のキーは [getValue] で例外にし、
 * 項目を追加した際に開始音・優先度が黙って別項目へフォールバックしないようにする。
 */
private val manualReadoutEventKeys: Map<String, LmuWindowsReadoutItemKey> =
    mapOf(
        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value to
            LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
        LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value to LmuWindowsReadoutItemKey.PitTiming.Root,
        LmuWindowsReadoutItemKey.PitTiming.TyreWear.value to LmuWindowsReadoutItemKey.PitTiming.Root,
        LmuWindowsReadoutItemKey.RemainingFuel.Laps.value to LmuWindowsReadoutItemKey.RemainingFuel.Root,
        LmuWindowsReadoutItemKey.RemainingFuel.Root.value to LmuWindowsReadoutItemKey.RemainingFuel.Root,
        LmuWindowsReadoutItemKey.ClassCarGap.Ahead.value to LmuWindowsReadoutItemKey.VehicleApproach.Root,
        LmuWindowsReadoutItemKey.ClassCarGap.Behind.value to LmuWindowsReadoutItemKey.VehicleApproach.Root,
        LmuWindowsReadoutItemKey.TyreWear.Root.value to LmuWindowsReadoutItemKey.TyreWear.Root,
        LmuWindowsReadoutItemKey.BrakeWear.Root.value to LmuWindowsReadoutItemKey.BrakeWear.Root,
        LmuWindowsReadoutItemKey.BrakeTemperature.Root.value to LmuWindowsReadoutItemKey.BrakeTemperature.Root,
        LmuWindowsReadoutItemKey.MyBestLap.Root.value to LmuWindowsReadoutItemKey.MyBestLap.Root,
        LmuWindowsReadoutItemKey.TyreTemperature.Root.value to LmuWindowsReadoutItemKey.TyreTemperature.Root,
    )

/** 項目キーから開始音・優先度に使う event キーを返す。未登録のキーは例外になる。 */
fun lmuWindowsManualReadoutEventKey(itemKey: String): LmuWindowsReadoutItemKey =
    manualReadoutEventKeys.getValue(itemKey)
