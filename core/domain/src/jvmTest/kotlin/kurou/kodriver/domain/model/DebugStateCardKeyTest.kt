package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class DebugStateCardKeyTest {

    @Test
    fun `SIMULATORとFLAG_INFOの2種類が定義されている`() {
        assertEquals(listOf(DebugStateCardKey.SIMULATOR, DebugStateCardKey.FLAG_INFO), DebugStateCardKey.entries)
    }
}
