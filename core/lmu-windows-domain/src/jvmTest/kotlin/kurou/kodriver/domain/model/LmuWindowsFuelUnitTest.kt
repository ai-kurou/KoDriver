package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LmuWindowsFuelUnitTest {
    @Test
    fun `valueは内部値を返す`() {
        assertEquals(42.0, LmuWindowsFuelUnit(42.0).value)
    }

    @Test
    fun `compareToは内部値の大小関係を返す`() {
        assertTrue(LmuWindowsFuelUnit(20.0) < LmuWindowsFuelUnit(50.0))
        assertTrue(LmuWindowsFuelUnit(50.0) > LmuWindowsFuelUnit(20.0))
        assertEquals(0, LmuWindowsFuelUnit(20.0).compareTo(LmuWindowsFuelUnit(20.0)))
    }
}
