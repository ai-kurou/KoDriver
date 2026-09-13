package kurou.kodriver.presentation

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.separatingHorizontalHingeBounds
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density

/**
 * テーブルトップ姿勢（水平ヒンジで半開き）の場合、ヒンジより下側は操作しづらいため、
 * コンテンツの高さをヒンジ上端までに制限する。テーブルトップ姿勢でない場合、または
 * 分離する水平ヒンジの情報が無い場合は何もしない。
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun Modifier.constrainToTabletopTopPane(
    posture: Posture,
    density: Density,
): Modifier {
    val topPanePx = posture.tabletopTopPaneHeightPx() ?: return this
    return heightIn(max = with(density) { topPanePx.toDp() })
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
