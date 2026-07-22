package kurou.kodriver.feature.debugstatedetail

import kotlin.test.Test
import kotlin.test.assertEquals

class LapTimeFormatterTest {

    @Test
    fun `1分23秒456ミリ秒は 1_23_456 形式で表示される`() {
        assertEquals("1:23.456", formatLapTimeMs(83_456L))
    }

    @Test
    fun `秒・ミリ秒がそれぞれ2桁・3桁未満の場合はゼロ埋めされる`() {
        assertEquals("1:02.005", formatLapTimeMs(62_005L))
    }

    @Test
    fun `0ミリ秒は 0_00_000 形式で表示される`() {
        assertEquals("0:00.000", formatLapTimeMs(0L))
    }
}
