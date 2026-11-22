package kurou.kodriver.core.designsystem

import kotlin.test.Test
import kotlin.test.assertEquals

class ReadoutBackgroundImageTest {
    @Test
    fun `背景画像の一辺は幅が狭いとき幅基準になる`() {
        // 幅基準 400 * 0.55 = 220 < 高さ基準 200 * 1.5 = 300
        assertEquals(220f, backgroundImageSide(width = 400f, height = 200f))
    }

    @Test
    fun `背景画像の一辺は幅が広いとき高さ基準の上限で止まる`() {
        // 幅基準 1200 * 0.55 = 660 > 高さ基準 200 * 1.5 = 300
        assertEquals(300f, backgroundImageSide(width = 1200f, height = 200f))
    }

    @Test
    fun `背景画像の一辺は幅基準と高さ基準が等しいとき、その値になる`() {
        // 幅基準 600 * 0.55 = 330 == 高さ基準 220 * 1.5 = 330
        assertEquals(330f, backgroundImageSide(width = 600f, height = 220f))
    }

    @Test
    fun `背景画像が1枚の領域幅は一辺と同じになる`() {
        assertEquals(backgroundImageSide(400f, 200f), backgroundImagesWidth(400f, 200f, 1))
    }

    @Test
    fun `背景画像が複数枚の領域幅は幅基準になる`() {
        // 幅基準 400 * 0.65 = 260 < 高さ基準 200 * 1.5 * 2 = 600
        assertEquals(260f, backgroundImagesWidth(400f, 200f, 2))
    }

    @Test
    fun `背景画像が複数枚の領域幅は幅が広いとき高さ基準の上限で止まる`() {
        // 幅基準 2000 * 0.65 = 1300 > 高さ基準 200 * 1.5 * 2 = 600
        assertEquals(600f, backgroundImagesWidth(2000f, 200f, 2))
    }
}
