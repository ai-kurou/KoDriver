package kurou.kodriver.domain.lmuwindows.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsClassCarGapReadoutTextTest {
    @Test
    fun `既定文言は秒数を小数1桁で展開する`() {
        assertEquals("クラス内前方{seconds}秒", LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_AHEAD_TEXT_DEFAULT)
        assertEquals("クラス内後方{seconds}秒", LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_BEHIND_TEXT_DEFAULT)
        assertEquals("クラス内トップです", LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TOP_TEXT_DEFAULT)
        assertEquals("クラス内最後尾です", LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_LAST_TEXT_DEFAULT)
        assertEquals(
            "クラス内前方1.5秒",
            formatLmuWindowsClassCarGapReadoutText(LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_AHEAD_TEXT_DEFAULT, 1.5),
        )
        assertEquals(
            emptyList(),
            findUnknownLmuWindowsClassCarGapReadoutPlaceholders(
                LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_AHEAD_TEXT_DEFAULT,
            ),
        )
    }

    @Test
    fun `秒数は小数1桁に丸めて整形する`() {
        mapOf(
            0.0 to "0.0",
            0.04 to "0.0",
            0.05 to "0.1",
            1.24 to "1.2",
            1.25 to "1.3",
            9.96 to "10.0",
            12.0 to "12.0",
            125.34 to "125.3",
        ).forEach { (seconds, expected) ->
            assertEquals(expected, formatLmuWindowsClassCarGapReadoutText("{seconds}", seconds))
        }
    }

    @Test
    fun `複数の既知トークンだけを置換し空白や未知トークンは維持する`() {
        assertEquals(" ", formatLmuWindowsClassCarGapReadoutText(" ", 1.0))
        assertEquals("差", formatLmuWindowsClassCarGapReadoutText("差", 1.0))
        assertEquals(
            "2.0・2.0・{unknown}",
            formatLmuWindowsClassCarGapReadoutText("{seconds}・{seconds}・{unknown}", 2.0),
        )
    }

    @Test
    fun `未知トークンを出現順で重複なく返す`() {
        assertEquals(
            listOf("{sec}", "{x}", "{}"),
            findUnknownLmuWindowsClassCarGapReadoutPlaceholders("{seconds}{sec}{x}{sec}{}"),
        )
        listOf("", "{seconds}", "{seconds}{seconds}", "{", "}", "{sec", "文言").forEach {
            assertEquals(emptyList(), findUnknownLmuWindowsClassCarGapReadoutPlaceholders(it))
        }
    }
}
