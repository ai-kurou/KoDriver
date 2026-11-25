package kurou.kodriver.core.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private const val TOP_FLOATING_ENTER_MILLIS = 300
private const val TOP_FLOATING_EXIT_MILLIS = 200

/** 一覧の上部に重ねる部品を、上からスライドしながらフェードして出入りさせる。位置は [modifier] で指定する。 */
@Composable
public fun KoDriverTopFloatingVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter =
            slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(durationMillis = TOP_FLOATING_ENTER_MILLIS),
            ) + fadeIn(animationSpec = tween(durationMillis = TOP_FLOATING_ENTER_MILLIS)),
        exit =
            slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(durationMillis = TOP_FLOATING_EXIT_MILLIS),
            ) + fadeOut(animationSpec = tween(durationMillis = TOP_FLOATING_EXIT_MILLIS)),
        modifier = modifier,
        content = content,
    )
}
