package kurou.kodriver.presentation

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * ハプティックフィードバック設定が無効なときに [androidx.compose.ui.platform.LocalHapticFeedback] へ
 * 差し替える何もしない実装。
 */
internal object NoOpHapticFeedback : HapticFeedback {
    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) = Unit
}
