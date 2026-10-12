package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AceWindowsFuelPercentTest {
    @Test
    fun `valueは内部値を返す`() {
        assertEquals(42.0, AceWindowsFuelPercent(42.0).value)
    }

    @Test
    fun `compareToは内部値の大小関係を返す`() {
        assertTrue(AceWindowsFuelPercent(20.0) < AceWindowsFuelPercent(50.0))
        assertTrue(AceWindowsFuelPercent(50.0) > AceWindowsFuelPercent(20.0))
        assertEquals(0, AceWindowsFuelPercent(20.0).compareTo(AceWindowsFuelPercent(20.0)))
    }
}
