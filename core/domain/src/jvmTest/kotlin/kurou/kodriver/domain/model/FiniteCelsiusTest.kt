package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class FiniteCelsiusTest {
    @Test
    fun `NaNと無限大を除いた温度を順序を保って返す`() {
        val readings =
            listOf(96f, Float.NaN, Float.POSITIVE_INFINITY, 0f, Float.NEGATIVE_INFINITY, -1f).map(::CelsiusReading)

        assertEquals(listOf(96f, 0f, -1f), readings.finiteCelsius())
    }

    @Test
    fun `有限値がなければ空を返す`() {
        assertEquals(emptyList(), listOf(CelsiusReading(Float.NaN)).finiteCelsius())
    }
}
