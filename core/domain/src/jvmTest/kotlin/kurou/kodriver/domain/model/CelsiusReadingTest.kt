package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CelsiusReadingTest {
    @Test
    fun `valueは内部値を返す`() {
        assertEquals(90.5, CelsiusReading(90.5).value)
    }

    @Test
    fun `compareToは内部値の大小関係を返す`() {
        assertTrue(CelsiusReading(90.0) < CelsiusReading(90.1))
        assertTrue(CelsiusReading(90.1) > CelsiusReading(90.0))
        assertEquals(0, CelsiusReading(90.0).compareTo(CelsiusReading(90.0)))
    }
}
