package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LmuWindowsPressureKpaTest {
    @Test
    fun `valueは内部値を返す`() {
        assertEquals(220.0, LmuWindowsPressureKpa(220.0).value)
    }

    @Test
    fun `compareToは内部値の大小関係を返す`() {
        assertTrue(LmuWindowsPressureKpa(200.0) < LmuWindowsPressureKpa(220.0))
        assertTrue(LmuWindowsPressureKpa(220.0) > LmuWindowsPressureKpa(200.0))
        assertEquals(0, LmuWindowsPressureKpa(200.0).compareTo(LmuWindowsPressureKpa(200.0)))
    }
}
