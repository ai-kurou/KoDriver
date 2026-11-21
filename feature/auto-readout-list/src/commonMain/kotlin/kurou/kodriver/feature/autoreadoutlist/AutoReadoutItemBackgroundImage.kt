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

/** 背景画像を置く幅の、項目カード幅に対する割合。画像は正方形のため右寄せで置く。 */
internal const val BACKGROUND_IMAGE_WIDTH_FRACTION = 0.55f

/** 背景画像の左端（カード色に溶け込む側）の不透明度。 */
internal const val BACKGROUND_IMAGE_FADE_START_ALPHA = 1f

/** 背景画像の右端での、カード色の不透明度。画像を暗く落として、文字やスイッチの視認性を保つ。 */
internal const val BACKGROUND_IMAGE_FADE_END_ALPHA = 0.3f

/** タイヤ関連の項目（タイヤ温度・タイヤ摩耗）は、カード右側にタイヤの画像を背景として表示する。 */
internal fun hasTyreBackgroundImage(item: ReadoutItemKey): Boolean =
    autoReadoutItemCategory(item) == ManualReadoutItemCategory.TYRE

/**
 * 正方形の [painter] を右寄せ・縦中央で描き、左端から [fadeColor]（カードの背景色）へ溶け込ませる。
 * 縦は描画領域からはみ出す分を切り取る。
 */
internal fun Modifier.rightAlignedBackgroundImage(
    painter: Painter,
    fadeColor: Color,
): Modifier =
    drawBehind {
        val side = size.width * BACKGROUND_IMAGE_WIDTH_FRACTION
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
