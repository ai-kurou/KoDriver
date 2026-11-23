package kurou.kodriver.feature.splash

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShineBandCenterTest {
    // 幅600・高さ100・帯100・傾き0.5 のとき pad = 150 + 0.5 * 100 * 100 / 100 = 200
    private fun center(progress: Float) = shineBandCenter(progress, 600f, 100f, 100f, 0.5f)

    @Test
    fun `進捗0では帯が画面の左外にある`() {
        assertEquals(-200f, center(0f))
    }

    @Test
    fun `進捗0_5では帯が画面の中央にある`() {
        assertEquals(300f, center(0.5f))
    }

    @Test
    fun `進捗1では帯が画面の右外にある`() {
        assertEquals(800f, center(1f))
    }

    @Test
    fun `縦に長い画面でも両端で画面の角に光が残らない`() {
        val width = 600f
        val height = 500f
        val band = 270f
        val slant = 0.5f
        val dx = band
        val dy = height * slant
        val length2 = dx * dx + dy * dy

        // 角のグラデーション位置 t（0..1 の外なら透明）
        fun t(
            progress: Float,
            x: Float,
            y: Float,
        ): Float {
            val c = shineBandCenter(progress, width, height, band, slant)
            return ((x - (c - band / 2f)) * dx + y * dy) / length2
        }
        // 開始時は全ての角がグラデーションの終点より先（t >= 1）、終了時は始点より手前（t <= 0）
        listOf(0f to 0f, width to 0f, 0f to height, width to height).forEach { (x, y) ->
            assertTrue(t(0f, x, y) >= 1f)
            assertTrue(t(1f, x, y) <= 0f)
        }
    }
}
