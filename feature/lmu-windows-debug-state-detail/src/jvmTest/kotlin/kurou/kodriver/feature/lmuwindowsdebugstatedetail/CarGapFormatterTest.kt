package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kotlin.test.Test
import kotlin.test.assertEquals

class CarGapFormatterTest {
    @Test
    fun `クラス内順位と車両数は整数で表示する`() {
        assertEquals("1", formatClassPosition(1))
        assertEquals("20", formatClassPosition(20))
        assertEquals("0", formatClassPosition(0))
    }

    @Test
    fun `クラス内順位と車両数のnullは欠損表示にする`() {
        assertEquals("--", formatClassPosition(null))
    }

    @Test
    fun `有限値は符号とゼロを保持して小数1桁で表示する`() {
        assertEquals("1.0", formatCarGapSeconds(1.0))
        assertEquals("1.3", formatCarGapSeconds(1.26))
        assertEquals("-2.3", formatCarGapSeconds(-2.34))
        assertEquals("0.0", formatCarGapSeconds(0.0))
        assertEquals("-0.0", formatCarGapSeconds(-0.0))
    }

    @Test
    fun `非有限値は欠損表示にする`() {
        assertEquals("--", formatCarGapSeconds(Double.NaN))
        assertEquals("--", formatCarGapSeconds(Double.POSITIVE_INFINITY))
        assertEquals("--", formatCarGapSeconds(Double.NEGATIVE_INFINITY))
    }

    @Test
    fun `nullは車両なしを表示する`() {
        assertEquals("なし", formatCarGapSeconds(null))
    }
}
