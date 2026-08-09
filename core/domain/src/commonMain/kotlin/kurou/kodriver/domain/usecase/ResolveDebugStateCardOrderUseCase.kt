package kurou.kodriver.domain.usecase

import kurou.kodriver.core.model.DebugStateCardKey

/**
 * 保存済みのカード順序と現在のデフォルト順序を突き合わせ、有効なカード順序を解決する。
 *
 * - 保存済み順序が空の場合はデフォルト順序をそのまま返す
 * - 保存済み順序のうちデフォルト順序に存在しない項目（削除済み項目）は除外する
 * - デフォルト順序にあり保存済み順序にない項目（新規項目）は末尾に補完する
 */
class ResolveDebugStateCardOrderUseCase {
    operator fun invoke(
        persistedOrder: List<DebugStateCardKey>,
        defaultOrder: List<DebugStateCardKey>,
    ): List<DebugStateCardKey> {
        if (persistedOrder.isEmpty()) return defaultOrder
        val ordered = persistedOrder.filter { it in defaultOrder }
        val appended = defaultOrder.filter { it !in persistedOrder }
        return ordered + appended
    }
}
