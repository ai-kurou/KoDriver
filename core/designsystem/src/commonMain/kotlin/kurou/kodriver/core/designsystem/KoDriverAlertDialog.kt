package kurou.kodriver.core.designsystem

import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * 呼び出し元の [LocalHapticFeedback] を各スロットへ引き継ぐ AlertDialog。
 *
 * ダイアログの内容は別ウィンドウで組み立てられ LocalHapticFeedback が上書きされるおそれがあるため、
 * ハプティックのオフ設定を効かせられるよう、AlertDialog の外側で読んだ値を内容へ渡す。
 * 振動はこの部品自身では発生させない。
 */
@Composable
public fun KoDriverAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current
    val provideHaptic: (@Composable () -> Unit) -> @Composable () -> Unit = { content ->
        {
            CompositionLocalProvider(LocalHapticFeedback provides haptic, content = content)
        }
    }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = provideHaptic(confirmButton),
        modifier = modifier,
        dismissButton = dismissButton?.let(provideHaptic),
        title = title?.let(provideHaptic),
        text = text?.let(provideHaptic),
    )
}
