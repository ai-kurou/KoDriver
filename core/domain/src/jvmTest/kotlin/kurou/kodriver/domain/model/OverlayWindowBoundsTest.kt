package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OverlayWindowBoundsTest {
    @Test
    fun `既定値は位置が未設定でサイズは既定のサイズ`() {
        val bounds = OverlayWindowBounds()

        assertFalse(bounds.isPositionSpecified)
        assertEquals(OVERLAY_WINDOW_WIDTH_DEFAULT, bounds.width)
        assertEquals(OVERLAY_WINDOW_HEIGHT_DEFAULT, bounds.height)
    }

    @Test
    fun `x・yの両方が設定されている場合のみ位置が設定済みになる`() {
        assertTrue(OverlayWindowBounds(x = 0, y = 0).isPositionSpecified)
        assertTrue(OverlayWindowBounds(x = -1920, y = -100).isPositionSpecified)
        assertFalse(OverlayWindowBounds(x = 100, y = OVERLAY_WINDOW_POSITION_UNSPECIFIED).isPositionSpecified)
        assertFalse(OverlayWindowBounds(x = OVERLAY_WINDOW_POSITION_UNSPECIFIED, y = 100).isPositionSpecified)
    }
}
