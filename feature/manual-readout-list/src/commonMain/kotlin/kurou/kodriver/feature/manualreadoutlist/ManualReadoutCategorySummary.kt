package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.runtime.Immutable
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
