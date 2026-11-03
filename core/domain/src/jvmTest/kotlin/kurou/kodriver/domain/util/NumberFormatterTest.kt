package kurou.kodriver.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals

class NumberFormatterTest {
    @Test
    fun `小数第2位を四捨五入して小数1桁にする`() {
        assertEquals("1.2", formatOneDecimal(1.24))
        assertEquals("1.3", formatOneDecimal(1.26))
        assertEquals("-1.3", formatOneDecimal(-1.26))
    }

    @Test
    fun `整数値は小数1桁で表示する`() {
        assertEquals("0.0", formatOneDecimal(0.0))
        assertEquals("12.0", formatOneDecimal(12.0))
    }
}
