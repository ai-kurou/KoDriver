package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LateralDistanceMetersTest {
    @Test
    fun `valueは内部値を返す`() {
        assertEquals(3.0, LateralDistanceMeters(3.0).value)
    }

    @Test
    fun `compareToは内部値の大小関係を返す`() {
        assertTrue(LateralDistanceMeters(1.0) < LateralDistanceMeters(2.0))
        assertTrue(LateralDistanceMeters(2.0) > LateralDistanceMeters(1.0))
        assertEquals(0, LateralDistanceMeters(1.0).compareTo(LateralDistanceMeters(1.0)))
    }
}
