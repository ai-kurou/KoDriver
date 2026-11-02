package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MeasurementValueTypesTest {
    @Test
    fun `valueは内部値を返す`() {
        assertEquals(0.036f, BrakeThicknessMeters(0.036f).value)
        assertEquals(90.5f, CelsiusReading(90.5f).value)
        assertEquals(90, Celsius(90).value)
        assertEquals(42.0, FuelPercent(42.0).value)
        assertEquals(3.0, LateralDistanceMeters(3.0).value)
        assertEquals(220.0, PressureKpa(220.0).value)
    }

    @Test
    fun `compareToは内部値の大小関係を返す`() {
        assertOrdering(BrakeThicknessMeters(0.030f), BrakeThicknessMeters(0.031f))
        assertOrdering(CelsiusReading(90.0f), CelsiusReading(90.1f))
        assertOrdering(Celsius(90), Celsius(100))
        assertOrdering(FuelPercent(20.0), FuelPercent(50.0))
        assertOrdering(LateralDistanceMeters(1.0), LateralDistanceMeters(2.0))
        assertOrdering(PressureKpa(200.0), PressureKpa(220.0))
    }

    @Test
    fun `Celsiusのminusは内部値の差を返す`() {
        assertEquals(Celsius(10), Celsius(100) - Celsius(90))
    }

    private fun <T : Comparable<T>> assertOrdering(
        smaller: T,
        larger: T,
    ) {
        assertTrue(smaller < larger, "$smaller < $larger")
        assertTrue(larger > smaller, "$larger > $smaller")
        assertEquals(0, smaller.compareTo(smaller), "$smaller compareTo itself")
    }
}
