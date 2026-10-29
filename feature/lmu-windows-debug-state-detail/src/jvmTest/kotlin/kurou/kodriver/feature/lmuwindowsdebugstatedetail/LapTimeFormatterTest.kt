package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kotlin.test.Test
import kotlin.test.assertEquals

class LapTimeFormatterTest {
    @Test
    fun `ラップタイムは秒を2桁・ミリ秒を3桁にゼロ埋めして分秒ミリ秒で表示する`() {
        assertEquals("1:23.456", formatLapTimeMs(83_456L))
        assertEquals("1:02.005", formatLapTimeMs(62_005L))
        assertEquals("0:00.000", formatLapTimeMs(0L))
    }
}
