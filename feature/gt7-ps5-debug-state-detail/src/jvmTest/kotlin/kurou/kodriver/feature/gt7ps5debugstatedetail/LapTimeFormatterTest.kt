package kurou.kodriver.feature.gt7ps5debugstatedetail

import kotlin.test.Test
import kotlin.test.assertEquals

class LapTimeFormatterTest {
    @Test
    fun `ラップタイムを分・秒・ミリ秒の形式でゼロ埋めして表示する`() {
        assertEquals("1:23.456", formatLapTimeMs(83_456L))
        assertEquals("1:02.005", formatLapTimeMs(62_005L))
        assertEquals("0:00.000", formatLapTimeMs(0L))
    }
}
