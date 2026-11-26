package kurou.kodriver.core.designsystem

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * ダイアログの確認・キャンセルに使うテキストボタン。タップ時は ContextClick ハプティックを発生させてから [onClick] を呼ぶ。
 *
 * [hapticFeedback] は既定で呼び出し位置の [LocalHapticFeedback] を使う。ダイアログ内ではその値が別ウィンドウのものになるため、
 * ダイアログ呼び出し側で外側に読んだ値を渡す。
 */
@Composable
public fun KoDriverDialogTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    hapticFeedback: HapticFeedback = LocalHapticFeedback.current,
) {
    TextButton(
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
    ) {
        Text(text)
    }
}
