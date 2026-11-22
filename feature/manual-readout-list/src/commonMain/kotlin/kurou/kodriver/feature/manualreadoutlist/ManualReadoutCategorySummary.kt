package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.runtime.Immutable
import kurou.kodriver.core.designsystem.ReadoutBackground
import kurou.kodriver.domain.model.ManualReadoutItemCategory

/** Compose が安定と判断できるよう、カテゴリ別件数の一覧を包む。 */
@Immutable
internal data class ManualReadoutCategorySummary(
    val counts: List<Pair<ManualReadoutItemCategory, Int>>,
)

/** 不明なキーを除き、カテゴリが最初に現れた順に集計する。 */
internal fun manualReadoutCategoryCounts(itemKeys: List<String>): List<Pair<ManualReadoutItemCategory, Int>> =
    itemKeys
        .mapNotNull(::manualReadoutItemCategory)
        .groupingBy { it }
        .eachCount()
        .toList()

internal fun manualReadoutCategorySummary(itemKeys: List<String>): ManualReadoutCategorySummary =
    ManualReadoutCategorySummary(manualReadoutCategoryCounts(itemKeys))

/** カテゴリに対応する背景画像を返す。 */
internal fun manualReadoutCategoryBackground(category: ManualReadoutItemCategory): ReadoutBackground =
    when (category) {
        ManualReadoutItemCategory.VIRTUAL_ENERGY -> ReadoutBackground.VIRTUAL_ENERGY
        ManualReadoutItemCategory.FUEL -> ReadoutBackground.FUEL
        ManualReadoutItemCategory.TYRE -> ReadoutBackground.TYRE
        ManualReadoutItemCategory.BRAKE -> ReadoutBackground.BRAKE
        ManualReadoutItemCategory.TIME -> ReadoutBackground.BEST_LAP
    }
