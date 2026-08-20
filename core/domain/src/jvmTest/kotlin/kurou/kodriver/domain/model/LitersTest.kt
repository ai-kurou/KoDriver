package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LitersTest {
    @Test
    fun `valueは内部値を返す`() {
        assertEquals(45.5, Liters(45.5).value)
    }

    @Test
    fun `compareToは内部値の大小関係を返す`() {
        assertTrue(Liters(45.5) < Liters(100.0))
        assertTrue(Liters(100.0) > Liters(45.5))
        assertEquals(0, Liters(45.5).compareTo(Liters(45.5)))
    }

    @Test
    fun `minusは内部値の差を返す`() {
        assertEquals(Liters(54.5), Liters(100.0) - Liters(45.5))
    }
}
