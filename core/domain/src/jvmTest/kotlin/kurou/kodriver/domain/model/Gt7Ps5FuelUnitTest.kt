package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Gt7Ps5FuelUnitTest {
    @Test
    fun `valueは内部値を返す`() {
        assertEquals(42f, Gt7Ps5FuelUnit(42f).value)
    }

    @Test
    fun `compareToは内部値の大小関係を返す`() {
        assertTrue(Gt7Ps5FuelUnit(20f) < Gt7Ps5FuelUnit(50f))
        assertTrue(Gt7Ps5FuelUnit(50f) > Gt7Ps5FuelUnit(20f))
        assertEquals(0, Gt7Ps5FuelUnit(20f).compareTo(Gt7Ps5FuelUnit(20f)))
    }

    @Test
    fun `plusは内部値の和を返す`() {
        assertEquals(Gt7Ps5FuelUnit(30f), Gt7Ps5FuelUnit(10f) + Gt7Ps5FuelUnit(20f))
    }

    @Test
    fun `minusは内部値の差を返す`() {
        assertEquals(Gt7Ps5FuelUnit(10f), Gt7Ps5FuelUnit(30f) - Gt7Ps5FuelUnit(20f))
    }

    @Test
    fun `Floatで割るとラップあたりの消費量を返す`() {
        assertEquals(Gt7Ps5FuelUnit(5f), Gt7Ps5FuelUnit(20f) / 4f)
    }

    @Test
    fun `Gt7Ps5FuelUnitで割ると無次元の比率を返す`() {
        assertEquals(4f, Gt7Ps5FuelUnit(20f) / Gt7Ps5FuelUnit(5f))
    }

    @Test
    fun `coerceAtLeastは下限値未満のとき下限値を返す`() {
        assertEquals(Gt7Ps5FuelUnit(0f), Gt7Ps5FuelUnit(-5f).coerceAtLeast(Gt7Ps5FuelUnit(0f)))
        assertEquals(Gt7Ps5FuelUnit(10f), Gt7Ps5FuelUnit(10f).coerceAtLeast(Gt7Ps5FuelUnit(0f)))
    }
}
