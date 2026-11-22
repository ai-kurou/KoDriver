package kurou.kodriver.feature.desktopsplash

import org.junit.Test
import kotlin.test.assertEquals

class ShineBandCenterTest {
    @Test
    fun `進捗0では帯が画面の左外にある`() {
        assertEquals(-100f, shineBandCenter(progress = 0f, width = 600f, bandWidth = 100f))
    }

    @Test
    fun `進捗0_5では帯が画面の中央にある`() {
        assertEquals(300f, shineBandCenter(progress = 0.5f, width = 600f, bandWidth = 100f))
    }

    @Test
    fun `進捗1では帯が画面の右外にある`() {
        assertEquals(700f, shineBandCenter(progress = 1f, width = 600f, bandWidth = 100f))
    }
}
