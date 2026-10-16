package kurou.kodriver.feature.manualreadoutdetail

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsManualReadoutHotkeySupportedTest {
    @Test
    fun `WindowsのOS名は大文字小文字に関係なく対応する`() {
        assertTrue(isWindowsOsName("Windows 11"))
        assertTrue(isWindowsOsName("windows 10"))
    }

    @Test
    fun `macOSとLinuxのOS名は対応しない`() {
        assertFalse(isWindowsOsName("Mac OS X"))
        assertFalse(isWindowsOsName("Linux"))
    }

    @Test
    fun `ホットキー対応は実行OSのWindows判定と一致する`() {
        assertEquals(isWindowsOsName(System.getProperty("os.name")), isManualReadoutHotkeySupported())
    }
}
