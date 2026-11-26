package kurou.kodriver.core.designsystem

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * ダイアログの確認・キャンセルに使うテキストボタン。タップ時は ContextClick ハプティックを発生させてから [onClick] を呼ぶ。
 */
@Composable
public fun KoDriverDialogTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current
    TextButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
    ) {
        Text(text)
    }
}
