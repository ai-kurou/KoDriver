package kurou.kodriver.core.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

private const val LIST_ENTRANCE_MILLIS = 200
private val ListEntranceOffset = 12.dp

/** 一覧を開いたときに、少し下からスライドしながらフェードインさせる。 */
@Composable
public fun KoDriverListEntrance(
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
    val entryOffset = with(LocalDensity.current) { ListEntranceOffset.roundToPx() }
    AnimatedVisibility(
        visibleState = visibleState,
        enter =
            slideInVertically(
                initialOffsetY = { entryOffset },
                animationSpec = tween(durationMillis = LIST_ENTRANCE_MILLIS),
            ) + fadeIn(animationSpec = tween(durationMillis = LIST_ENTRANCE_MILLIS)),
        modifier = modifier,
        content = content,
    )
}
