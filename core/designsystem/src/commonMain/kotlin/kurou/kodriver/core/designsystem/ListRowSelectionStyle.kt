package kurou.kodriver.core.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val SELECTION_ANIMATION_MS = 500

/** 一覧の行カードの、選択状態に応じた見た目。 */
public data class ListRowSelectionStyle(
    public val containerColor: Color,
    public val borderColor: Color,
    public val shadowElevation: Dp,
)

/** 選択中は secondaryContainer の背景・primary の枠線・影にし、切り替えをアニメーションする。 */
@Composable
public fun animateListRowSelectionStyle(isSelected: Boolean): ListRowSelectionStyle {
    val colors = MaterialTheme.colorScheme
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) colors.secondaryContainer else colors.surfaceContainerLow,
        animationSpec = tween(durationMillis = SELECTION_ANIMATION_MS),
        label = "selectionContainerColor",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) colors.primary else Color.Transparent,
        animationSpec = tween(durationMillis = SELECTION_ANIMATION_MS),
        label = "selectionBorderColor",
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 0.dp,
        animationSpec = tween(durationMillis = SELECTION_ANIMATION_MS),
        label = "selectionShadowElevation",
    )
    return ListRowSelectionStyle(containerColor, borderColor, shadowElevation)
}

/** [style] の影と枠線を付ける。背景色はカード側に [ListRowSelectionStyle.containerColor] を渡す。 */
@Composable
public fun Modifier.listRowSelection(
    style: ListRowSelectionStyle,
    shape: Shape,
): Modifier {
    val primary = MaterialTheme.colorScheme.primary
    return shadow(
        elevation = style.shadowElevation,
        shape = shape,
        ambientColor = primary,
        spotColor = primary,
    ).border(1.dp, style.borderColor, shape)
}
