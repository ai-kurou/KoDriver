package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsMyBestLapReadoutTextTest {
    @Test
    fun `既定文言は更新後のタイムを日本語で展開する`() {
        val template = ACE_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT
        assertEquals("自己ベストラップ更新 {laptime}", template)
        assertEquals("自己ベストラップ更新 1分23秒005", formatAceWindowsMyBestLapReadoutText(template, 83_005))
        assertEquals(emptyList(), findUnknownAceWindowsMyBestLapReadoutPlaceholders(template))
    }

    @Test
    fun `0から999ミリ秒は分を省略しミリ秒を3桁で読む`() {
        for (milliseconds in 0..999) {
            assertEquals(
                "0秒${milliseconds.toString().padStart(3, '0')}",
                formatAceWindowsMyBestLapReadoutText("{laptime}", milliseconds),
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
            assertEquals(expected, formatAceWindowsMyBestLapReadoutText("{laptime}", time))
        }
    }

    @Test
    fun `複数の既知トークンだけを置換し空白や未知トークンは維持する`() {
        assertEquals(" ", formatAceWindowsMyBestLapReadoutText(" ", 5))
        assertEquals("更新", formatAceWindowsMyBestLapReadoutText("更新", 5))
        assertEquals(
            "0秒005・0秒005・{unknown}",
            formatAceWindowsMyBestLapReadoutText("{laptime}・{laptime}・{unknown}", 5),
        )
    }

    @Test
    fun `未知トークンを出現順で重複なく返す`() {
        assertEquals(
            listOf("{lap}", "{x}", "{}"),
            findUnknownAceWindowsMyBestLapReadoutPlaceholders("{laptime}{lap}{x}{lap}{}"),
        )
        listOf("", "{laptime}", "{laptime}{laptime}", "{", "}", "{lap", "文言").forEach {
            assertEquals(emptyList(), findUnknownAceWindowsMyBestLapReadoutPlaceholders(it))
        }
        assertEquals(listOf("{x}"), findUnknownAceWindowsMyBestLapReadoutPlaceholders("{{x}}"))
    }
}
