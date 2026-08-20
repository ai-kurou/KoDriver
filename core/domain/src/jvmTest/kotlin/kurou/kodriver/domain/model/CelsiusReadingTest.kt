package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CelsiusReadingTest {
    @Test
    fun `valueは内部値を返す`() {
        assertEquals(90.5f, CelsiusReading(90.5f).value)
    }

    @Test
    fun `compareToは内部値の大小関係を返す`() {
        assertTrue(CelsiusReading(90.0f) < CelsiusReading(90.1f))
        assertTrue(CelsiusReading(90.1f) > CelsiusReading(90.0f))
        assertEquals(0, CelsiusReading(90.0f).compareTo(CelsiusReading(90.0f)))
    }
}
