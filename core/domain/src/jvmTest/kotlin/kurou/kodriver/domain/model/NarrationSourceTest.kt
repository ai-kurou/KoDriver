package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NarrationSourceTest {
    @Test
    fun `fromIdは対応するenumを返す`() {
        assertEquals(NarrationSource.AUTO, NarrationSource.fromId("auto"))
        assertEquals(NarrationSource.MANUAL, NarrationSource.fromId("manual"))
    }

    @Test
    fun `fromIdは未知のidにnullを返す`() {
        assertNull(NarrationSource.fromId("unknown"))
    }

    @Test
    fun `idは全て一意である`() {
        assertEquals(
            NarrationSource.entries.size,
            NarrationSource.entries
                .map { it.id }
                .toSet()
                .size,
        )
    }
}
