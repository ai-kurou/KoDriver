package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import kotlin.test.Test
import kotlin.test.assertEquals

class BrakeWearValueFormatterTest {
    @Test
    fun `指数表記にならず小数8桁で整形する`() {
        assertEquals("0.03600000", formatBrakeWearValue(0.036))
        assertEquals("0.00000001", formatBrakeWearValue(1.0E-8))
        assertEquals("12.50000000", formatBrakeWearValue(12.5))
    }

    @Test
    fun `負の値は符号付きで整形する`() {
        assertEquals("-0.00100000", formatBrakeWearValue(-0.001))
    }

    @Test
    fun `小数8桁で0に丸まる負の値は符号を付けない`() {
        assertEquals("0.00000000", formatBrakeWearValue(-1.0E-12))
        assertEquals("0.00000000", formatBrakeWearValue(0.0))
    }

    @Test
    fun `差分は増加のときだけプラス符号を付ける`() {
        assertEquals("+0.00100000", formatBrakeWearDelta(0.001))
        assertEquals("-0.00100000", formatBrakeWearDelta(-0.001))
    }

    @Test
    fun `差分が0または0に丸まる場合は符号を付けない`() {
        assertEquals("0.00000000", formatBrakeWearDelta(0.0))
        assertEquals("0.00000000", formatBrakeWearDelta(1.0E-12))
    }
}
