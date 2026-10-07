package kurou.kodriver.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * 非同期保存の古い値による入力の巻き戻りを防ぐ文言の状態。
 * 正規化は保存側UseCaseの `trim().take(MAX)` と一致させる必要がある。
 */
@Stable
class PendingTextState internal constructor(
    savedText: String,
    private val maxLength: Int,
) {
    var currentText by mutableStateOf(savedText)
        private set

    internal var pendingText by mutableStateOf<String?>(null)
        private set

    /** 入力・リセットした文言を表示し、正規化後の保存値を待つ。 */
    fun change(text: String) {
        currentText = text
        pendingText = text.trim().take(maxLength)
    }

    internal fun updateSavedText(savedText: String) {
        if (pendingText == null) {
            currentText = savedText
        } else if (pendingText == savedText) {
            pendingText = null
        }
    }
}

/**
 * 入力中の文言を保持し、正規化後の保存値が一致したら保存待ちを解除する。
 */
@Composable
fun rememberPendingText(
    savedText: String,
    maxLength: Int,
): PendingTextState {
    val state = remember { PendingTextState(savedText, maxLength) }
    LaunchedEffect(savedText, state.pendingText) {
        state.updateSavedText(savedText)
    }
    return state
}
