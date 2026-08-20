package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CelsiusTest {
    @Test
    fun `valueは内部値を返す`() {
        assertEquals(90, Celsius(90).value)
    }

    @Test
    fun `compareToは内部値の大小関係を返す`() {
        assertTrue(Celsius(90) < Celsius(100))
        assertTrue(Celsius(100) > Celsius(90))
        assertEquals(0, Celsius(90).compareTo(Celsius(90)))
    }

    @Test
    fun `minusは内部値の差を返す`() {
        assertEquals(Celsius(10), Celsius(100) - Celsius(90))
    }
}
