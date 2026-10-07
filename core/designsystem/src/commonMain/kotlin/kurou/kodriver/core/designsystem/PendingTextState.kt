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
 * 保存反映がconflateされ途中の値を観測できない場合、保存待ちが残ることがある。
 */
@Stable
class PendingTextState internal constructor(
    savedText: String,
    private val maxLength: Int,
) {
    var currentText by mutableStateOf(savedText)
        private set

    private var pendingTexts by mutableStateOf<List<String>>(emptyList())

    private var lastSaved by mutableStateOf(savedText)

    /** 入力・リセットした文言を表示し、正規化後の保存値を待つ。 */
    fun change(text: String) {
        currentText = text
        val normalizedText = text.trim().take(maxLength)
        if (pendingTexts.isNotEmpty() || normalizedText != lastSaved) {
            pendingTexts = pendingTexts + normalizedText
        } else {
            // 保存値が変わらず反映は来ないため、正規化後の保存値をそのまま表示する。
            currentText = lastSaved
        }
    }

    internal fun updateSavedText(savedText: String) {
        if (savedText == lastSaved) return
        lastSaved = savedText
        if (pendingTexts.isEmpty()) {
            currentText = savedText
        } else {
            val lastMatch = pendingTexts.lastIndexOf(savedText)
            if (lastMatch >= 0) {
                pendingTexts = pendingTexts.drop(lastMatch + 1)
                if (pendingTexts.isEmpty()) {
                    currentText = savedText
                }
            }
        }
    }
}

/**
 * 入力中の文言を保持し、正規化後の保存値を順に観測して保存待ちを解除する。
 */
@Composable
fun rememberPendingText(
    savedText: String,
    maxLength: Int,
): PendingTextState {
    val state = remember { PendingTextState(savedText, maxLength) }
    LaunchedEffect(savedText) {
        state.updateSavedText(savedText)
    }
    return state
}
