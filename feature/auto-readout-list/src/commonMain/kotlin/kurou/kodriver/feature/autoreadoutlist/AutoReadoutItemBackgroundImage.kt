package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import kurou.kodriver.domain.model.ManualReadoutItemCategory
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.feature.autoreadoutlist.generated.resources.Res
import kurou.kodriver.feature.autoreadoutlist.generated.resources.best_lap_row_background
import kurou.kodriver.feature.autoreadoutlist.generated.resources.brake_row_background
import kurou.kodriver.feature.autoreadoutlist.generated.resources.tyre_row_background
import org.jetbrains.compose.resources.DrawableResource

/**
 * 背景画像を置く幅の、項目カード幅に対する割合。画像は正方形のため右寄せで置く。
 * ただし一辺は高さ基準の上限 [BACKGROUND_IMAGE_MAX_HEIGHT_FRACTION] を超えない。
 */
internal const val BACKGROUND_IMAGE_WIDTH_FRACTION = 0.55f

/** 背景画像の一辺の、項目カード高さに対する上限の倍率。幅が広いときに画像が大きくなりすぎるのを防ぐ。 */
internal const val BACKGROUND_IMAGE_MAX_HEIGHT_FRACTION = 1.5f

/** 背景画像の左端（カード色に溶け込む側）の不透明度。 */
internal const val BACKGROUND_IMAGE_FADE_START_ALPHA = 1f

/** 背景画像の右端での、カード色の不透明度。画像を暗く落として、文字やスイッチの視認性を保つ。 */
internal const val BACKGROUND_IMAGE_FADE_END_ALPHA = 0.3f

/**
 * 背景画像の一辺（px）を返す。幅基準（[BACKGROUND_IMAGE_WIDTH_FRACTION]）と高さ基準の上限
 * （[BACKGROUND_IMAGE_MAX_HEIGHT_FRACTION]）の小さい方。drawBehind 内はユニットテストできないため切り出している。
 */
internal fun backgroundImageSide(
    width: Float,
    height: Float,
): Float = minOf(width * BACKGROUND_IMAGE_WIDTH_FRACTION, height * BACKGROUND_IMAGE_MAX_HEIGHT_FRACTION)

/**
 * 項目のカード右側に背景として表示する画像を返す。画像を持たない項目は null。
 * タイヤ関連（温度・摩耗）はタイヤ、ブレーキ関連（温度・摩耗）はブレーキ、自己ベストラップはドライバーの画像を使う。
 */
internal fun autoReadoutBackgroundImage(item: ReadoutItemKey): DrawableResource? =
    when (autoReadoutItemCategory(item)) {
        ManualReadoutItemCategory.TYRE -> Res.drawable.tyre_row_background
        ManualReadoutItemCategory.BRAKE -> Res.drawable.brake_row_background
        ManualReadoutItemCategory.TIME -> Res.drawable.best_lap_row_background
        else -> null
    }

/**
 * 正方形の [painter] を右寄せ・縦中央で描き、左端から [fadeColor]（カードの背景色）へ溶け込ませる。
 * 一辺は [backgroundImageSide] で決め、幅が広くても [BACKGROUND_IMAGE_MAX_HEIGHT_FRACTION] を超えて大きくならない。
 * 縦は描画領域からはみ出す分を切り取る。
 */
internal fun Modifier.rightAlignedBackgroundImage(
    painter: Painter,
    fadeColor: Color,
): Modifier =
    drawBehind {
        val side = backgroundImageSide(size.width, size.height)
        val left = size.width - side
        clipRect(left = left) {
            translate(left = left, top = (size.height - side) / 2f) {
                with(painter) { draw(Size(side, side)) }
            }
        }
        drawRect(
            brush =
                Brush.horizontalGradient(
                    colors =
                        listOf(
                            fadeColor.copy(alpha = BACKGROUND_IMAGE_FADE_START_ALPHA),
                            fadeColor.copy(alpha = BACKGROUND_IMAGE_FADE_END_ALPHA),
                        ),
                    startX = left,
                    endX = size.width,
                ),
            topLeft = Offset(left, 0f),
            size = Size(side, size.height),
        )
    }
