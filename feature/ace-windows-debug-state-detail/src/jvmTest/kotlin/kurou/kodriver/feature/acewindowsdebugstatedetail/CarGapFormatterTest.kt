package kurou.kodriver.feature.acewindowsdebugstatedetail

import kotlin.test.Test
import kotlin.test.assertEquals

class CarGapFormatterTest {
    @Test
    fun `有限値は符号とゼロを保持して小数1桁で表示する`() {
        assertEquals("1.0", formatCarGapSeconds(1.0f))
        assertEquals("1.3", formatCarGapSeconds(1.26f))
        assertEquals("-2.3", formatCarGapSeconds(-2.34f))
        assertEquals("0.0", formatCarGapSeconds(0.0f))
        assertEquals("-0.0", formatCarGapSeconds(-0.0f))
    }

    @Test
    fun `非有限値は欠損表示にする`() {
        assertEquals("--", formatCarGapSeconds(Float.NaN))
        assertEquals("--", formatCarGapSeconds(Float.POSITIVE_INFINITY))
        assertEquals("--", formatCarGapSeconds(Float.NEGATIVE_INFINITY))
    }

    @Test
    fun `総合順位と総台数は正の値だけ数値で表示する`() {
        assertEquals("1", formatOverallPosition(1))
        assertEquals("20", formatOverallPosition(20))
        assertEquals("--", formatOverallPosition(0))
        assertEquals("--", formatOverallPosition(-1))
        assertEquals("--", formatOverallPosition(null))
    }
}
