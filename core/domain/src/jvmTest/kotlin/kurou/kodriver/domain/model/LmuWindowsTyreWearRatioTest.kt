package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LmuWindowsTyreWearRatioTest {
    @Test
    fun `valueは内部値を返す`() {
        assertEquals(0.8, LmuWindowsTyreWearRatio(0.8).value)
    }

    @Test
    fun `compareToは内部値の大小関係を返す`() {
        assertTrue(LmuWindowsTyreWearRatio(0.2) < LmuWindowsTyreWearRatio(0.5))
        assertTrue(LmuWindowsTyreWearRatio(0.5) > LmuWindowsTyreWearRatio(0.2))
        assertEquals(0, LmuWindowsTyreWearRatio(0.2).compareTo(LmuWindowsTyreWearRatio(0.2)))
    }
}
