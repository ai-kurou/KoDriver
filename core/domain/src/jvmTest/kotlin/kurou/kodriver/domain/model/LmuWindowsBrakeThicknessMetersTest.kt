package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LmuWindowsBrakeThicknessMetersTest {
    @Test
    fun `valueは内部値を返す`() {
        assertEquals(0.036f, LmuWindowsBrakeThicknessMeters(0.036f).value)
    }

    @Test
    fun `compareToは内部値の大小関係を返す`() {
        assertTrue(LmuWindowsBrakeThicknessMeters(0.030f) < LmuWindowsBrakeThicknessMeters(0.031f))
        assertTrue(LmuWindowsBrakeThicknessMeters(0.031f) > LmuWindowsBrakeThicknessMeters(0.030f))
        assertEquals(0, LmuWindowsBrakeThicknessMeters(0.030f).compareTo(LmuWindowsBrakeThicknessMeters(0.030f)))
    }
}
