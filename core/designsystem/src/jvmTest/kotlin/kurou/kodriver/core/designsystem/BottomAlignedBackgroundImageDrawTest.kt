package kurou.kodriver.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BottomAlignedBackgroundImageDrawTest {
    private val imageColor = Color.Red
    private val fadeColor = Color.Blue

    private fun drawPixels(
        width: Dp,
        height: Dp,
        block: (PixelMap) -> Unit,
    ) = runComposeUiTest {
        setContent {
            Box(
                modifier =
                    Modifier
                        .requiredSize(width, height)
                        .background(Color.White)
                        .bottomAlignedBackgroundImage(ColorPainter(imageColor), fadeColor),
            )
        }
        block(onRoot().captureToImage().toPixelMap())
    }

    @Test
    fun `幅が狭いとき画像は下端に幅いっぱいで置かれ、上端から色へ溶ける`() =
        drawPixels(100.dp, 200.dp) { pixels ->
            assertEquals(100, pixels.width)
            assertEquals(200, pixels.height)
            // 一辺 = min(100, 200 * 0.6) = 100。画像の上端は y = 100
            assertEquals(Color.White, pixels[50, 50])
            assertTrue(pixels[50, 100].isNear(fadeColor), "画像の上端はカード色: ${pixels[50, 100]}")
            // 下端はカード色が薄く、画像の赤が勝つ
            val bottom = pixels[50, 199]
            assertTrue(bottom.red > bottom.blue, "下端は画像の色が強い: $bottom")
            assertTrue(bottom.blue > 0f, "下端にもカード色が残る: $bottom")
            // 上へ向かうほどカード色が強い
            assertTrue(pixels[50, 120].blue > pixels[50, 180].blue)
        }

    @Test
    fun `幅が広いとき画像は横中央に高さ基準の上限で置かれ、両脇は描かない`() =
        drawPixels(800.dp, 500.dp) { pixels ->
            // 一辺 = min(800, 500 * 0.6) = 300。画像は x = 250..550、上端は y = 200。
            // 色の重ねは幅全体にかかり、画像は縦だけ切り取るため、両脇はカード色だけが載る
            assertEquals(Color.White, pixels[400, 100])
            assertTrue(pixels[400, 200].isNear(fadeColor), "上端はカード色: ${pixels[400, 200]}")
            val side = pixels[100, 450]
            val center = pixels[400, 450]
            assertEquals(side.red, side.green, "両脇に画像の赤は載らない")
            assertTrue(center.green < side.green, "中央には画像の赤が載る: center=$center side=$side")
            assertTrue(center.red > center.blue, "下端は画像の色が強い: $center")
        }

    private fun Color.isNear(other: Color): Boolean =
        abs(red - other.red) < COLOR_TOLERANCE &&
            abs(green - other.green) < COLOR_TOLERANCE &&
            abs(blue - other.blue) < COLOR_TOLERANCE

    private companion object {
        const val COLOR_TOLERANCE = 0.02f
    }
}
