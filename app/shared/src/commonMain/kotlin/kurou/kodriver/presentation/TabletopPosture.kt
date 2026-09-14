package kurou.kodriver.presentation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.separatingHorizontalHingeBounds
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import kotlin.math.roundToInt

/**
 * テーブルトップ姿勢（水平ヒンジで半開き）の場合、ヒンジより下側は操作しづらいため、
 * コンテンツの高さをヒンジ上端までに制限する。テーブルトップ姿勢でない場合、または
 * 分離する水平ヒンジの情報が無い場合は何もしない。
 *
 * `heightIn(max = ...)` は incoming constraints の minHeight を下回る max を
 * 指定できない（呼び出し元やさらに祖先の `fillMaxSize()` 等で minHeight が
 * 既に画面いっぱいに固定されていると、指定した max が無視される）ため使用しない。
 * ここでは `Modifier.layout` で子の測定に渡す constraints を直接上書きし、
 * minHeight もヒンジ上端の高さ以下に coerce したうえで maxHeight を強制する。
 * これにより、子が `fillMaxSize()` で自身を最大まで広げようとする場合はヒンジ上端の
 * 高さちょうどになり、子がそれより小さいコンテンツであれば元の意図通りのサイズになる。
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun Modifier.constrainToTabletopTopPane(posture: Posture): Modifier {
    val topPanePx = posture.tabletopTopPaneHeightPx() ?: return this
    return this.then(
        Modifier.layout { measurable, constraints ->
            val maxHeightPx = topPanePx.roundToInt().coerceAtMost(constraints.maxHeight)
            val childConstraints =
                constraints.copy(
                    minHeight = constraints.minHeight.coerceAtMost(maxHeightPx),
                    maxHeight = maxHeightPx,
                )
            val placeable = measurable.measure(childConstraints)
            layout(placeable.width, placeable.height) {
                placeable.place(0, 0)
            }
        },
    )
}

/**
 * テーブルトップ姿勢における、ヒンジより上側（操作可能な上半分）の高さを px で返す。
 * テーブルトップ姿勢でない場合、または分離する水平ヒンジの情報が無い場合は null を返す。
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
internal fun Posture.tabletopTopPaneHeightPx(): Float? {
    if (!isTabletop) return null
    return separatingHorizontalHingeBounds.firstOrNull()?.top
}
