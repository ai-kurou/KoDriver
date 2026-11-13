package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.ui.graphics.Color
import kurou.kodriver.core.designsystem.ExtendedColorScheme
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutItemCategory
import kurou.kodriver.domain.model.ReadoutItemKey

/**
 * 自動タブの項目が属する読み上げカテゴリを返す。手動タブ・音声タブと同じカテゴリ色にそろえるために使う。
 * 燃料・タイヤ・ブレーキ・ラップなどカテゴリを持たない項目（フラッグ、車両接近など）は null を返し、従来どおりの配色にする。
 */
internal fun autoReadoutItemCategory(itemId: ReadoutItemKey): ManualReadoutItemCategory? =
    when (itemId) {
        is LmuWindowsReadoutItemKey -> lmuWindowsItemCategory(itemId)
        is Gt7Ps5ReadoutItemKey -> gt7Ps5ItemCategory(itemId)
        is AceWindowsReadoutItemKey -> aceWindowsItemCategory(itemId)
        else -> null
    }

private fun lmuWindowsItemCategory(itemId: LmuWindowsReadoutItemKey): ManualReadoutItemCategory? =
    when (itemId) {
        is LmuWindowsReadoutItemKey.RemainingVirtualEnergy -> ManualReadoutItemCategory.VIRTUAL_ENERGY
        is LmuWindowsReadoutItemKey.RemainingFuel -> ManualReadoutItemCategory.FUEL
        is LmuWindowsReadoutItemKey.TyreTemperature -> ManualReadoutItemCategory.TYRE
        is LmuWindowsReadoutItemKey.TyreWear -> ManualReadoutItemCategory.TYRE
        is LmuWindowsReadoutItemKey.BrakeTemperature -> ManualReadoutItemCategory.BRAKE
        is LmuWindowsReadoutItemKey.BrakeWear -> ManualReadoutItemCategory.BRAKE
        is LmuWindowsReadoutItemKey.MyBestLap -> ManualReadoutItemCategory.LAP
        is LmuWindowsReadoutItemKey.VehicleApproach, is LmuWindowsReadoutItemKey.Flag -> null
        is LmuWindowsReadoutItemKey.VehicleDamage, is LmuWindowsReadoutItemKey.PitTiming -> null
        is LmuWindowsReadoutItemKey.ClassCarGap -> null
    }

private fun gt7Ps5ItemCategory(itemId: Gt7Ps5ReadoutItemKey): ManualReadoutItemCategory =
    when (itemId) {
        is Gt7Ps5ReadoutItemKey.MyBestLap -> ManualReadoutItemCategory.LAP
        is Gt7Ps5ReadoutItemKey.RemainingFuelLaps -> ManualReadoutItemCategory.FUEL
        is Gt7Ps5ReadoutItemKey.RemainingFuel -> ManualReadoutItemCategory.FUEL
        is Gt7Ps5ReadoutItemKey.TyreTemperature -> ManualReadoutItemCategory.TYRE
    }

private fun aceWindowsItemCategory(itemId: AceWindowsReadoutItemKey): ManualReadoutItemCategory? =
    when (itemId) {
        is AceWindowsReadoutItemKey.RemainingFuel -> ManualReadoutItemCategory.FUEL
        is AceWindowsReadoutItemKey.RemainingFuelLaps -> ManualReadoutItemCategory.FUEL
        is AceWindowsReadoutItemKey.TyreTemperature -> ManualReadoutItemCategory.TYRE
        is AceWindowsReadoutItemKey.MyBestLap -> ManualReadoutItemCategory.LAP
        is AceWindowsReadoutItemKey.VehicleApproach, is AceWindowsReadoutItemKey.Flag -> null
    }

/** 手動タブ・音声タブと同じ、読み上げカテゴリ専用の拡張カラー。 */
internal fun autoReadoutCategoryColor(
    category: ManualReadoutItemCategory,
    colors: ExtendedColorScheme,
): Color =
    when (category) {
        ManualReadoutItemCategory.VIRTUAL_ENERGY -> colors.categoryVirtualEnergy
        ManualReadoutItemCategory.FUEL -> colors.categoryFuel
        ManualReadoutItemCategory.TYRE -> colors.categoryTyre
        ManualReadoutItemCategory.BRAKE -> colors.categoryBrake
        ManualReadoutItemCategory.LAP -> colors.categoryLap
    }
