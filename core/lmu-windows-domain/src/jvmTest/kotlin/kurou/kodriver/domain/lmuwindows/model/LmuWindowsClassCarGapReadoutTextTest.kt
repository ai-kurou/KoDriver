package kurou.kodriver.domain.lmuwindows.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsClassCarGapReadoutTextTest {
    @Test
    fun `既定文言は前方の後に後方を読み上げる`() {
        assertEquals("{ahead}、{behind}", LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TEXT_DEFAULT)
        assertEquals("クラス内先頭です", LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TOP_TEXT_DEFAULT)
        assertEquals("クラス内最後尾です", LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_LAST_TEXT_DEFAULT)
        assertEquals(
            "クラス内前方1.5秒、後方2.0秒",
            formatLmuWindowsClassCarGapReadoutText(
                LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TEXT_DEFAULT,
                formatLmuWindowsClassCarGapAheadPhrase(1.5),
                formatLmuWindowsClassCarGapBehindPhrase(2.0),
            ),
        )
        assertEquals(
            emptyList(),
            findUnknownLmuWindowsClassCarGapReadoutPlaceholders(LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TEXT_DEFAULT),
        )
    }

    @Test
    fun `プレースホルダーは入れ替えや片方だけでも置換される`() {
        assertEquals(
            "後方B、前方A",
            formatLmuWindowsClassCarGapReadoutText("{behind}、{ahead}", "前方A", "後方B"),
        )
        assertEquals("前方A", formatLmuWindowsClassCarGapReadoutText("{ahead}", "前方A", "後方B"))
    }

    @Test
    fun `未知のプレースホルダーを出現順・重複なしで返す`() {
        assertEquals(
            listOf("{seconds}", "{x}"),
            findUnknownLmuWindowsClassCarGapReadoutPlaceholders("{ahead}{seconds}{behind}{x}{seconds}"),
        )
    }

    @Test
    fun `プレビューは前方と後方を同じ秒差で展開する`() {
        assertEquals(
            "クラス内前方1.5秒、後方1.5秒",
            formatLmuWindowsClassCarGapPreviewText(LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TEXT_DEFAULT, 1.5),
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
            assertEquals("クラス内前方${expected}秒", formatLmuWindowsClassCarGapAheadPhrase(seconds))
            assertEquals("後方${expected}秒", formatLmuWindowsClassCarGapBehindPhrase(seconds))
        }
    }
}
