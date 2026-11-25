package kurou.kodriver.core.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment

private const val COLLAPSIBLE_ENTER_MILLIS = 300
private const val COLLAPSIBLE_EXIT_MILLIS = 200

/**
 * 周囲のレイアウトが飛ばないよう、高さを伸縮させながらフェードして出入りする。非表示中は子要素を配置しない。
 * 親の `Arrangement.spacedBy` は高さ0の子要素にも間隔を足すため、退場アニメーション完了後は取り除く。
 * 最初から [visible] が true の場合はアニメーションせずに表示する。
 *
 * [expandFrom] は伸縮の基準にする端で、出るときと消えるときの両方に使う。
 */
@Composable
public fun KoDriverCollapsibleVisibility(
    visible: Boolean,
    expandFrom: Alignment.Vertical = Alignment.Top,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    val visibleState = remember { MutableTransitionState(visible) }
    visibleState.targetState = visible
    if (visibleState.currentState || visibleState.targetState) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter =
                fadeIn(tween(COLLAPSIBLE_ENTER_MILLIS)) +
                    expandVertically(tween(COLLAPSIBLE_ENTER_MILLIS), expandFrom = expandFrom),
            exit =
                fadeOut(tween(COLLAPSIBLE_EXIT_MILLIS)) +
                    shrinkVertically(tween(COLLAPSIBLE_EXIT_MILLIS), shrinkTowards = expandFrom),
            content = content,
        )
    }
}
