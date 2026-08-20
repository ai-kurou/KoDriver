package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelPercentTest {
    @Test
    fun `valueは内部値を返す`() {
        assertEquals(42.0, FuelPercent(42.0).value)
    }

    @Test
    fun `compareToは内部値の大小関係を返す`() {
        assertTrue(FuelPercent(20.0) < FuelPercent(50.0))
        assertTrue(FuelPercent(50.0) > FuelPercent(20.0))
        assertEquals(0, FuelPercent(20.0).compareTo(FuelPercent(20.0)))
    }
}
