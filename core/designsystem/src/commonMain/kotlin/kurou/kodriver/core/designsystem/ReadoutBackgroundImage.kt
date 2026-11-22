package kurou.kodriver.core.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter

/**
 * 背景画像を置く幅の、項目カード幅に対する割合。画像は正方形のため右寄せで置く。
 * ただし一辺は高さ基準の上限 [BACKGROUND_IMAGE_MAX_HEIGHT_FRACTION] を超えない。
 */
internal const val BACKGROUND_IMAGE_WIDTH_FRACTION = 0.55f

/** 背景画像の一辺の、項目カード高さに対する上限の倍率。幅が広いときに画像が大きくなりすぎるのを防ぐ。 */
internal const val BACKGROUND_IMAGE_MAX_HEIGHT_FRACTION = 1.5f

/** 背景画像を2枚以上横に並べるときの、項目カード幅に対する並べた領域全体の幅の割合。 */
internal const val BACKGROUND_IMAGES_WIDTH_FRACTION = 0.65f

/** 背景画像を全面に敷くときに、画像の上へ重ねる色の不透明度。上に置く文字やアイコンの視認性を保つ。 */
internal const val FULL_BACKGROUND_IMAGE_OVERLAY_ALPHA = 0.65f

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
 * 背景画像を [count] 枚横に並べた領域の幅（px）を返す。1枚のときは [backgroundImageSide] と同じ。
 * 2枚以上は [BACKGROUND_IMAGES_WIDTH_FRACTION] を幅基準とし、1枚あたり高さ基準の上限を超えない。
 */
internal fun backgroundImagesWidth(
    width: Float,
    height: Float,
    count: Int,
): Float =
    if (count <= 1) {
        backgroundImageSide(width, height)
    } else {
        minOf(width * BACKGROUND_IMAGES_WIDTH_FRACTION, height * BACKGROUND_IMAGE_MAX_HEIGHT_FRACTION * count)
    }

/**
 * 正方形の [painter] を右寄せ・縦中央で描き、左端から [fadeColor]（カードの背景色）へ溶け込ませる。
 * 一辺は [backgroundImageSide] で決め、幅が広くても [BACKGROUND_IMAGE_MAX_HEIGHT_FRACTION] を超えて大きくならない。
 * 縦は描画領域からはみ出す分を切り取る。
 */
public fun Modifier.rightAlignedBackgroundImage(
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

/**
 * 正方形の [painters] を右寄せで横に等分して並べ、各画像を縦横中央で切り取って描く。
 * 左端から [fadeColor]（カードの背景色）へ溶け込ませる描き方は [rightAlignedBackgroundImage] と同じ。
 * 1枚のときは [rightAlignedBackgroundImage] と同じ見た目になる。
 */
public fun Modifier.rightAlignedBackgroundImages(
    painters: List<Painter>,
    fadeColor: Color,
): Modifier =
    if (painters.size <= 1) {
        painters.firstOrNull()?.let { rightAlignedBackgroundImage(it, fadeColor) } ?: this
    } else {
        drawBehind {
            val total = backgroundImagesWidth(size.width, size.height, painters.size)
            val left = size.width - total
            val tileWidth = total / painters.size
            val side = maxOf(tileWidth, size.height)
            painters.forEachIndexed { index, painter ->
                val tileLeft = left + tileWidth * index
                clipRect(left = tileLeft, right = tileLeft + tileWidth) {
                    translate(left = tileLeft + (tileWidth - side) / 2f, top = (size.height - side) / 2f) {
                        with(painter) { draw(Size(side, side)) }
                    }
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
                size = Size(total, size.height),
            )
        }
    }

/**
 * 正方形の [painters] を領域全体に横へ等分して敷き、各画像を縦横中央で切り取って描く。
 * 画像の上に [overlayColor] を [FULL_BACKGROUND_IMAGE_OVERLAY_ALPHA] で重ね、前面の文字やアイコンを読みやすくする。
 * [painters] が空のときは何も描かない。
 */
public fun Modifier.fullBackgroundImages(
    painters: List<Painter>,
    overlayColor: Color,
): Modifier =
    if (painters.isEmpty()) {
        this
    } else {
        drawBehind {
            val tileWidth = size.width / painters.size
            val side = maxOf(tileWidth, size.height)
            painters.forEachIndexed { index, painter ->
                val tileLeft = tileWidth * index
                clipRect(left = tileLeft, right = tileLeft + tileWidth) {
                    translate(left = tileLeft + (tileWidth - side) / 2f, top = (size.height - side) / 2f) {
                        with(painter) { draw(Size(side, side)) }
                    }
                }
            }
            drawRect(color = overlayColor.copy(alpha = FULL_BACKGROUND_IMAGE_OVERLAY_ALPHA))
        }
    }
