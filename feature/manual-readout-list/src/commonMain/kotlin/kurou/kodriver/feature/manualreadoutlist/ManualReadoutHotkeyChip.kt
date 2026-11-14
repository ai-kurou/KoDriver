package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.feature.manualreadoutlist.generated.resources.Res
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_hotkey_unassigned
import org.jetbrains.compose.resources.stringResource

// ホットキーチップの出現・消失で行の高さが変わるのを滑らかにする。アプリのバナーと同じ300ms/200ms。
internal val ManualReadoutHotkeyChipEnter: EnterTransition =
    fadeIn(tween(durationMillis = 300)) + expandVertically(tween(durationMillis = 300), expandFrom = Alignment.Top)

internal val ManualReadoutHotkeyChipExit: ExitTransition =
    fadeOut(tween(durationMillis = 200)) + shrinkVertically(tween(durationMillis = 200), shrinkTowards = Alignment.Top)

/**
 * 非表示中は子要素を配置しない [AnimatedVisibility]。
 * 親の `Arrangement.spacedBy` は高さ0の子要素にも間隔を足すため、退場アニメーション完了後は取り除く。
 */
@Composable
internal fun ManualReadoutHotkeyChipAnimatedVisibility(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    val visibleState = remember { MutableTransitionState(visible) }
    visibleState.targetState = visible
    if (visibleState.currentState || visibleState.targetState) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = ManualReadoutHotkeyChipEnter,
            exit = ManualReadoutHotkeyChipExit,
            content = content,
        )
    }
}

/** 行の操作を妨げない、表示専用のホットキーチップ。 */
@Composable
internal fun ManualReadoutHotkeyChip(
    set: ManualReadoutSet,
    playButtonLayout: Boolean = false,
) {
    val label = manualReadoutHotkeyLabel(set.hotkeyModifiers, set.hotkeyVirtualKeyCode)
    val borderColor = if (label == null) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.outline
    Surface(
        shape = MaterialTheme.shapes.small,
        color =
            if (playButtonLayout) {
                MaterialTheme.colorScheme.primaryContainer
            } else if (label == null) {
                Color.Transparent
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
        contentColor =
            if (playButtonLayout) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else if (label == null) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            },
        border = BorderStroke(1.dp, borderColor),
    ) {
        Text(
            text = label ?: stringResource(Res.string.manual_readout_list_hotkey_unassigned),
            style = MaterialTheme.typography.labelSmall,
            modifier =
                Modifier
                    .drawBehind {
                        if (label != null) {
                            drawLine(borderColor, Offset(0f, size.height), Offset(size.width, size.height), 3.dp.toPx())
                        }
                    }.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
        )
    }
}
