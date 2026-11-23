package kurou.kodriver.feature.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.ReadoutBackground
import kurou.kodriver.core.designsystem.rightAlignedBackgroundImage
import kurou.kodriver.feature.splash.generated.resources.Res
import kurou.kodriver.feature.splash.generated.resources.app_icon
import org.jetbrains.compose.resources.painterResource

/** 光の帯の幅の、画面幅に対する割合。 */
internal const val SHINE_BAND_WIDTH_FRACTION = 0.45f

/** 光の帯が最も濃いところの不透明度。 */
internal const val SHINE_PEAK_ALPHA = 0.35f

/** 光の帯の傾きを決める、グラデーション終点の y 座標の画面高さに対する割合。 */
private const val SHINE_BAND_SLANT_FRACTION = 0.5f

/** スプラッシュに表示するアプリアイコンの標準の大きさ。 */
public val SplashAppIconSize: Dp = 96.dp

/**
 * 光の帯の中心の x 座標（px）を返す。[progress] が 0 のとき帯は画面の左外、1 のとき右外にあり、
 * 画面全体を左から右へ横切る。drawWithContent 内はユニットテストできないため切り出している。
 *
 * 帯は斜め（グラデーションの終点が [height] × [slantFraction] だけ下）なので、画面の角でも
 * 両端で光が残らないよう、斜めの分（角をグラデーションの向きへ射影した長さ）を移動量に含める。
 */
internal fun shineBandCenter(
    progress: Float,
    width: Float,
    height: Float,
    bandWidth: Float,
    slantFraction: Float,
): Float {
    val pad = 1.5f * bandWidth + slantFraction * height * height / bandWidth
    return -pad + (width + 2f * pad) * progress
}

/**
 * 光が画面を横切る演出の描画。[progress] が 0 以下または 1 以上のときは何も描かない。
 * 帯は斜めのグラデーションで、色は [color] を中央で [SHINE_PEAK_ALPHA] まで濃くする。
 */
private fun Modifier.shine(
    progress: Float,
    color: Color,
): Modifier =
    drawWithContent {
        drawContent()
        if (progress > 0f && progress < 1f) {
            val bandWidth = size.width * SHINE_BAND_WIDTH_FRACTION
            val center = shineBandCenter(progress, size.width, size.height, bandWidth, SHINE_BAND_SLANT_FRACTION)
            drawRect(
                brush =
                    Brush.linearGradient(
                        colors =
                            listOf(
                                Color.Transparent,
                                color.copy(alpha = SHINE_PEAK_ALPHA),
                                Color.Transparent,
                            ),
                        start = Offset(center - bandWidth / 2f, 0f),
                        end = Offset(center + bandWidth / 2f, size.height * SHINE_BAND_SLANT_FRACTION),
                    ),
            )
        }
    }

/**
 * スプラッシュ画面の背景。自己ベストラップの写真を右側に置いて左へ背景色へ溶かし、その上に光の帯を重ねる。
 * [shineProgress]（0〜1）は光の位置で、範囲外のときは光を描かない。[content] は背景の上に重ねる。
 * デスクトップと Android のスプラッシュで共通に使う。
 */
@Composable
public fun SplashBackdrop(
    shineProgress: Float,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val surface = MaterialTheme.colorScheme.surface
    val photo = painterResource(ReadoutBackground.BEST_LAP.drawable)
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .rightAlignedBackgroundImage(photo, surface)
                .shine(shineProgress, MaterialTheme.colorScheme.primary),
        content = content,
    )
}

/** スプラッシュに表示するアプリアイコン。大きさは [size] で変えられる。 */
@Composable
public fun SplashAppIcon(
    modifier: Modifier = Modifier,
    size: Dp = SplashAppIconSize,
) {
    Image(
        painter = painterResource(Res.drawable.app_icon),
        contentDescription = "KoDriver",
        modifier = modifier.size(size),
    )
}
