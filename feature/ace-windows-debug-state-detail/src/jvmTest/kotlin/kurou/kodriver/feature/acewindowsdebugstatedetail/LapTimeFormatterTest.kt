package kurou.kodriver.feature.acewindowsdebugstatedetail

import kotlin.test.Test
import kotlin.test.assertEquals

class LapTimeFormatterTest {
    @Test
    fun `ラップタイムは秒・ミリ秒をゼロ埋めして分_秒_ミリ秒形式で表示する`() {
        assertEquals("1:23.456", formatLapTimeMs(83_456L))
        assertEquals("1:02.005", formatLapTimeMs(62_005L))
        assertEquals("0:00.000", formatLapTimeMs(0L))
    }
}
