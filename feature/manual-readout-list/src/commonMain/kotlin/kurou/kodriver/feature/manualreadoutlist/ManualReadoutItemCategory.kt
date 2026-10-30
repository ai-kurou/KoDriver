package kurou.kodriver.feature.manualreadoutlist

import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey

internal enum class ManualReadoutItemCategory {
    VIRTUAL_ENERGY,
    FUEL,
    TYRE,
    BRAKE,
    LAP,
}

/** detail の itemPresentations と同じ手動読み上げ項目のみを分類する。 */
private val manualReadoutItemCategories =
    mapOf(
        LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value to ManualReadoutItemCategory.VIRTUAL_ENERGY,
        LmuWindowsReadoutItemKey.RemainingFuel.Laps.value to ManualReadoutItemCategory.FUEL,
        LmuWindowsReadoutItemKey.PitTiming.TyreWear.value to ManualReadoutItemCategory.TYRE,
        LmuWindowsReadoutItemKey.MyBestLap.Root.value to ManualReadoutItemCategory.LAP,
        AceWindowsReadoutItemKey.MyBestLap.Root.value to ManualReadoutItemCategory.LAP,
        LmuWindowsReadoutItemKey.ClassCarGap.Ahead.value to ManualReadoutItemCategory.LAP,
        LmuWindowsReadoutItemKey.ClassCarGap.Behind.value to ManualReadoutItemCategory.LAP,
        LmuWindowsReadoutItemKey.RemainingFuel.Root.value to ManualReadoutItemCategory.FUEL,
        AceWindowsReadoutItemKey.RemainingFuel.Root.value to ManualReadoutItemCategory.FUEL,
        AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value to ManualReadoutItemCategory.FUEL,
        Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value to ManualReadoutItemCategory.FUEL,
        Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value to ManualReadoutItemCategory.FUEL,
        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value to ManualReadoutItemCategory.VIRTUAL_ENERGY,
        Gt7Ps5ReadoutItemKey.MyBestLap.Root.value to ManualReadoutItemCategory.LAP,
        AceWindowsReadoutItemKey.TyreTemperature.Root.value to ManualReadoutItemCategory.TYRE,
        Gt7Ps5ReadoutItemKey.TyreTemperature.Root.value to ManualReadoutItemCategory.TYRE,
        LmuWindowsReadoutItemKey.TyreWear.Root.value to ManualReadoutItemCategory.TYRE,
        LmuWindowsReadoutItemKey.BrakeWear.Root.value to ManualReadoutItemCategory.BRAKE,
        LmuWindowsReadoutItemKey.BrakeTemperature.Root.value to ManualReadoutItemCategory.BRAKE,
        LmuWindowsReadoutItemKey.TyreTemperature.Root.value to ManualReadoutItemCategory.TYRE,
    )

internal fun manualReadoutItemCategory(key: String): ManualReadoutItemCategory? = manualReadoutItemCategories[key]
