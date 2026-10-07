package kurou.kodriver.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

internal const val PENDING_TEXT_TIMEOUT_MILLIS_DEFAULT = 3000L

/**
 * 非同期保存の古い値による入力の巻き戻りを防ぐ文言の状態。
 * 正規化は保存側UseCaseの `trim().take(MAX)` と一致させる必要がある。
 * 保存反映が conflate されて途中の値を観測できない場合は、タイムアウトで保存待ちを解除する。
 */
@Stable
class PendingTextState internal constructor(
    savedText: String,
    private val maxLength: Int,
) {
    var currentText by mutableStateOf(savedText)
        private set

    internal var pendingTexts by mutableStateOf<List<String>>(emptyList())
        private set

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

    /** 保存待ちの期限が過ぎても入力中の文言は保持する。 */
    internal fun expirePending() {
        pendingTexts = emptyList()
    }

    internal fun updateSavedText(savedText: String) {
        if (savedText == lastSaved) return
        lastSaved = savedText
        if (pendingTexts.isEmpty()) {
            currentText = savedText
        } else {
            val firstMatch = pendingTexts.indexOf(savedText)
            if (firstMatch >= 0) {
                pendingTexts = pendingTexts.drop(firstMatch + 1)
                if (pendingTexts.isEmpty()) {
                    currentText = savedText
                }
            }
        }
    }
}

/**
 * 入力中の文言を保持し、正規化後の保存値を順に観測して保存待ちを解除する。
 * 保存待ちが変化してから [pendingTimeoutMillis] 経過すると、入力を保持したまま保存待ちを解除する。
 */
@Composable
fun rememberPendingText(
    savedText: String,
    maxLength: Int,
    pendingTimeoutMillis: Long = PENDING_TEXT_TIMEOUT_MILLIS_DEFAULT,
): PendingTextState {
    val state = remember { PendingTextState(savedText, maxLength) }
    LaunchedEffect(savedText) {
        state.updateSavedText(savedText)
    }
    val pendingTexts = state.pendingTexts
    LaunchedEffect(pendingTexts, pendingTimeoutMillis) {
        if (pendingTexts.isNotEmpty()) {
            delay(pendingTimeoutMillis)
            state.expirePending()
        }
    }
    return state
}
