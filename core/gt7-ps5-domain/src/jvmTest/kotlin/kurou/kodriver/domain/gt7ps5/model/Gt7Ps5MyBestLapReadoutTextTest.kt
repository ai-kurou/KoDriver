package kurou.kodriver.domain.gt7ps5.model

import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5MyBestLapReadoutTextTest {
    @Test
    fun `既定文言は更新後のタイムを日本語で展開する`() {
        val template = GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT
        assertEquals("自己ベストラップ更新 {laptime}", template)
        assertEquals("自己ベストラップ更新 1分23秒005", formatGt7Ps5MyBestLapReadoutText(template, 83_005))
        assertEquals(emptyList(), findUnknownGt7Ps5MyBestLapReadoutPlaceholders(template))
    }

    @Test
    fun `0から999ミリ秒は分を省略しミリ秒を3桁で読む`() {
        for (milliseconds in 0..999) {
            assertEquals(
                "0秒${milliseconds.toString().padStart(3, '0')}",
                formatGt7Ps5MyBestLapReadoutText("{laptime}", milliseconds),
            )
        }
    }

    @Test
    fun `秒と分の境界および60分以上を整形する`() {
        mapOf(
            1_000 to "1秒000",
            23_456 to "23秒456",
            59_999 to "59秒999",
            60_000 to "1分0秒000",
            83_456 to "1分23秒456",
            120_005 to "2分0秒005",
            4_512_345 to "75分12秒345",
            Int.MAX_VALUE to "35791分23秒647",
        ).forEach { (time, expected) ->
            assertEquals(expected, formatGt7Ps5MyBestLapReadoutText("{laptime}", time))
        }
    }
}
