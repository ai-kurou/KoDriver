package kurou.kodriver.domain.lmuwindows.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsClassCarGapReadoutTextTest {
    @Test
    fun `既定文言は前方と後方の節の組で固定文言と「なし」の定数を持つ`() {
        assertEquals("前方{ahead}秒、後方{behind}秒", LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TEXT_DEFAULT)
        assertEquals("前方{ahead}秒、後方{behind}秒", LMU_WINDOWS_CLASS_CAR_GAP_PLACEHOLDERS)
        assertEquals("前方{ahead}秒", LMU_WINDOWS_CLASS_CAR_GAP_AHEAD_CLAUSE)
        assertEquals("後方{behind}秒", LMU_WINDOWS_CLASS_CAR_GAP_BEHIND_CLAUSE)
        assertEquals("クラス内先頭です", LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TOP_TEXT_DEFAULT)
        assertEquals("クラス内最後尾です", LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_LAST_TEXT_DEFAULT)
        assertEquals("なし", LMU_WINDOWS_CLASS_CAR_GAP_NONE_VALUE_TEXT)
        assertEquals(
            emptyList(),
            findUnknownLmuWindowsClassCarGapReadoutPlaceholders(LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TEXT_DEFAULT),
        )
    }

    @Test
    fun `秒数があるときはプレースホルダーを単位なしの秒数だけに置換する`() {
        assertEquals(
            "前方1.5秒、後方2.0秒",
            formatLmuWindowsClassCarGapReadoutText(
                LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TEXT_DEFAULT,
                aheadSeconds = "1.5",
                behindSeconds = "2.0",
            ),
        )
        assertEquals(
            "2.0、1.5",
            formatLmuWindowsClassCarGapReadoutText("{behind}、{ahead}", aheadSeconds = "1.5", behindSeconds = "2.0"),
        )
    }

    @Test
    fun `先頭のときは前方の節全体を固定文言に置換する`() {
        assertEquals(
            "クラス内先頭です、後方2.0秒",
            formatLmuWindowsClassCarGapReadoutText(
                LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TEXT_DEFAULT,
                aheadSeconds = null,
                behindSeconds = "2.0",
            ),
        )
    }

    @Test
    fun `最後尾のときは後方の節全体を固定文言に置換する`() {
        assertEquals(
            "前方1.5秒、クラス内最後尾です",
            formatLmuWindowsClassCarGapReadoutText(
                LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TEXT_DEFAULT,
                aheadSeconds = "1.5",
                behindSeconds = null,
            ),
        )
    }

    @Test
    fun `節を含まない自由文言で車なしのときはプレースホルダー単体を「なし」に置換する`() {
        assertEquals(
            "前方なし",
            formatLmuWindowsClassCarGapReadoutText(
                "前方{ahead}",
                aheadSeconds = null,
                behindSeconds = "2.0",
            ),
        )
        assertEquals(
            "後方なし",
            formatLmuWindowsClassCarGapReadoutText(
                "後方{behind}",
                aheadSeconds = "1.5",
                behindSeconds = null,
            ),
        )
        assertEquals(
            "なし、なし",
            formatLmuWindowsClassCarGapReadoutText("{ahead}、{behind}", aheadSeconds = null, behindSeconds = null),
        )
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
            "前方1.5秒、後方1.5秒",
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
            assertEquals(expected, formatClassCarGapSeconds(seconds))
        }
    }
}
