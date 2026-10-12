package kurou.kodriver.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals

class ResolveDebugStateCardOrderUseCaseTest {
    private val useCase = ResolveDebugStateCardOrderUseCase()

    @Test
    fun `保存済み順序が空の場合はデフォルト順序を返す`() {
        val default = listOf("SIMULATOR", "FLAG_INFO", "SESSION")

        assertEquals(default, useCase(persistedOrder = emptyList(), defaultOrder = default))
    }

    @Test
    fun `保存済み順序を維持しつつ削除済み項目を除外し新規項目を末尾に補完する`() {
        val persisted = listOf("SESSION", "BEST_LAP", "SIMULATOR")
        val default = listOf("SIMULATOR", "FLAG_INFO", "SESSION")

        val result = useCase(persistedOrder = persisted, defaultOrder = default)

        assertEquals(
            listOf("SESSION", "SIMULATOR", "FLAG_INFO"),
            result,
        )
    }

    @Test
    fun `文字列以外のキーでも順序を解決できる`() {
        assertEquals(listOf(3, 1, 2), useCase(persistedOrder = listOf(3, 4, 1), defaultOrder = listOf(1, 2, 3)))
    }
}
