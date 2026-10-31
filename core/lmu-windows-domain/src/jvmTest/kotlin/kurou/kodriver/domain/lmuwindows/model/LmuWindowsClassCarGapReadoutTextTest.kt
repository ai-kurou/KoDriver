package kurou.kodriver.domain.lmuwindows.model

import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsClassCarGapReadoutTextTest {
    @Test
    fun `既定文言は秒数を小数1桁で展開する`() {
        assertEquals("クラス内前方{seconds}秒", LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_AHEAD_TEXT_DEFAULT)
        assertEquals("クラス内後方{seconds}秒", LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_BEHIND_TEXT_DEFAULT)
        assertEquals("クラス内先頭です", LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TOP_TEXT_DEFAULT)
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
}
