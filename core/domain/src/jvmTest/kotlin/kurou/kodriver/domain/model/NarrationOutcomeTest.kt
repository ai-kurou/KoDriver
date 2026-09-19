package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NarrationOutcomeTest {
    @Test
    fun `fromIdは対応するenumを返す`() {
        assertEquals(NarrationOutcome.QUEUED, NarrationOutcome.fromId("queued"))
        assertEquals(NarrationOutcome.INTERRUPTED, NarrationOutcome.fromId("interrupted"))
        assertEquals(NarrationOutcome.SKIPPED, NarrationOutcome.fromId("skipped"))
    }

    @Test
    fun `fromIdは未知のidにnullを返す`() {
        assertNull(NarrationOutcome.fromId("unknown"))
    }

    @Test
    fun `idは全て一意である`() {
        assertEquals(NarrationOutcome.entries.size, NarrationOutcome.entries.map { it.id }.toSet().size)
    }
}
