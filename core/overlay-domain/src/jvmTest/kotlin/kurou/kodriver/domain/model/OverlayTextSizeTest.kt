package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class OverlayTextSizeTest {
    @Test
    fun `idからオーバーレイ文字サイズへ変換できる`() {
        assertEquals(OverlayTextSize.EXTRA_SMALL, OverlayTextSize.fromId("extra_small"))
        assertEquals(OverlayTextSize.SMALL, OverlayTextSize.fromId("small"))
        assertEquals(OverlayTextSize.MEDIUM, OverlayTextSize.fromId("medium"))
        assertEquals(OverlayTextSize.LARGE, OverlayTextSize.fromId("large"))
        assertEquals(OverlayTextSize.EXTRA_LARGE, OverlayTextSize.fromId("extra_large"))
        assertEquals(OverlayTextSize.HUGE, OverlayTextSize.fromId("huge"))
        assertEquals(OverlayTextSize.MAXIMUM, OverlayTextSize.fromId("maximum"))
        OverlayTextSize.entries.forEach { overlayTextSize ->
            assertEquals(overlayTextSize, OverlayTextSize.fromId(overlayTextSize.id))
        }
    }

    @Test
    fun `未知のidはMEDIUMになる`() {
        assertEquals(OverlayTextSize.MEDIUM, OverlayTextSize.fromId("unknown"))
    }
}
