package kurou.kodriver.feature.otherlist

import kotlin.test.Test
import kotlin.test.assertEquals

class OtherListOverlayTextColorLabelTest {
    @Test
    fun `RGBを大文字の6桁で表示しアルファを除く`() {
        assertEquals("#FFC107", 0xFFFFC107L.toRgbHexLabel())
        assertEquals("#123ABC", 0x80123ABCL.toRgbHexLabel())
        assertEquals("#123ABC", 0x00123ABCL.toRgbHexLabel())
        assertEquals("#000000", 0L.toRgbHexLabel())
        assertEquals("#000001", 1L.toRgbHexLabel())
        assertEquals("#FFFFFF", 0xFFFFFFFFL.toRgbHexLabel())
    }
}
