package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class OverlayTextSizeTest {
    @Test
    fun `idからオーバーレイ文字サイズへ変換できる`() {
        assertEquals(OverlayTextSize.SMALL, OverlayTextSize.fromId("small"))
        assertEquals(OverlayTextSize.MEDIUM, OverlayTextSize.fromId("medium"))
        assertEquals(OverlayTextSize.LARGE, OverlayTextSize.fromId("large"))
    }

    @Test
    fun `未知のidはMEDIUMになる`() {
        assertEquals(OverlayTextSize.MEDIUM, OverlayTextSize.fromId("unknown"))
    }
}
