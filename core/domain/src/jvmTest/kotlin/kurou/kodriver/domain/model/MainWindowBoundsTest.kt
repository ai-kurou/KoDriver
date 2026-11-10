package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MainWindowBoundsTest {
    @Test
    fun `既定値は位置が未設定でサイズは既定のサイズ`() {
        val bounds = MainWindowBounds()

        assertFalse(bounds.isPositionSpecified)
        assertEquals(MAIN_WINDOW_WIDTH_DEFAULT, bounds.width)
        assertEquals(MAIN_WINDOW_HEIGHT_DEFAULT, bounds.height)
    }

    @Test
    fun `x・yの両方が設定されている場合のみ位置が設定済みになる`() {
        assertTrue(MainWindowBounds(x = 0, y = 0).isPositionSpecified)
        assertTrue(MainWindowBounds(x = -1920, y = -100).isPositionSpecified)
        assertFalse(MainWindowBounds(x = 100, y = MAIN_WINDOW_POSITION_UNSPECIFIED).isPositionSpecified)
        assertFalse(MainWindowBounds(x = MAIN_WINDOW_POSITION_UNSPECIFIED, y = 100).isPositionSpecified)
    }
}
