package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PressureKpaTest {
    @Test
    fun `valueは内部値を返す`() {
        assertEquals(220.0, PressureKpa(220.0).value)
    }

    @Test
    fun `compareToは内部値の大小関係を返す`() {
        assertTrue(PressureKpa(200.0) < PressureKpa(220.0))
        assertTrue(PressureKpa(220.0) > PressureKpa(200.0))
        assertEquals(0, PressureKpa(200.0).compareTo(PressureKpa(200.0)))
    }
}
