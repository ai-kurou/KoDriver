package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrakeThicknessMetersTest {
    @Test
    fun `valueは内部値を返す`() {
        assertEquals(0.036f, BrakeThicknessMeters(0.036f).value)
    }

    @Test
    fun `compareToは内部値の大小関係を返す`() {
        assertTrue(BrakeThicknessMeters(0.030f) < BrakeThicknessMeters(0.031f))
        assertTrue(BrakeThicknessMeters(0.031f) > BrakeThicknessMeters(0.030f))
        assertEquals(0, BrakeThicknessMeters(0.030f).compareTo(BrakeThicknessMeters(0.030f)))
    }
}
