package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.DebugStateCardKey
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolveDebugStateCardOrderUseCaseTest {
    private val useCase = ResolveDebugStateCardOrderUseCase()

    @Test
    fun `保存済み順序が空の場合はデフォルト順序を返す`() {
        val default = listOf(DebugStateCardKey.SIMULATOR, DebugStateCardKey.FLAG_INFO, DebugStateCardKey.SESSION)

        assertEquals(default, useCase(persistedOrder = emptyList(), defaultOrder = default))
    }

    @Test
    fun `保存済み順序を維持しつつ削除済み項目を除外し新規項目を末尾に補完する`() {
        val persisted = listOf(DebugStateCardKey.SESSION, DebugStateCardKey.BEST_LAP, DebugStateCardKey.SIMULATOR)
        val default = listOf(DebugStateCardKey.SIMULATOR, DebugStateCardKey.FLAG_INFO, DebugStateCardKey.SESSION)

        val result = useCase(persistedOrder = persisted, defaultOrder = default)

        assertEquals(
            listOf(DebugStateCardKey.SESSION, DebugStateCardKey.SIMULATOR, DebugStateCardKey.FLAG_INFO),
            result,
        )
    }

    @Test
    fun `保存済み順序の全項目が削除済みの場合はデフォルト順序のみ返す`() {
        val persisted = listOf(DebugStateCardKey.BEST_LAP)
        val default = listOf(DebugStateCardKey.SIMULATOR, DebugStateCardKey.FLAG_INFO)

        assertEquals(
            listOf(DebugStateCardKey.SIMULATOR, DebugStateCardKey.FLAG_INFO),
            useCase(persistedOrder = persisted, defaultOrder = default),
        )
    }
}
