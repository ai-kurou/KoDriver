package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LmuWindowsLateralDistanceMetersTest {
    @Test
    fun `valueは内部値を返す`() {
        assertEquals(3.0, LmuWindowsLateralDistanceMeters(3.0).value)
    }

    @Test
    fun `compareToは内部値の大小関係を返す`() {
        assertTrue(LmuWindowsLateralDistanceMeters(1.0) < LmuWindowsLateralDistanceMeters(2.0))
        assertTrue(LmuWindowsLateralDistanceMeters(2.0) > LmuWindowsLateralDistanceMeters(1.0))
        assertEquals(0, LmuWindowsLateralDistanceMeters(1.0).compareTo(LmuWindowsLateralDistanceMeters(1.0)))
    }
}
