package kurou.kodriver.presentation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class VersionMismatchBottomSheetContentTest {
    @get:Rule
    val rule = createComposeRule()

    private val haptic: HapticFeedback = mockk()
    private val onDismiss: () -> Unit = mockk()

    @Test
    fun `表示だけではハプティックと閉じる操作を実行しない`() {
        show()

        rule.waitForIdle()

        verify(exactly = 0) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        verify(exactly = 0) { onDismiss() }
        confirmVerified(haptic, onDismiss)
    }

    @Test
    fun `閉じるボタンを押すとハプティックと閉じる操作をそれぞれ一度実行する`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        every { onDismiss() } returns Unit
        show()

        rule.onNodeWithText("閉じる").performClick()

        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        verify(exactly = 1) { onDismiss() }
        confirmVerified(haptic, onDismiss)
    }

    @Test
    fun `ハプティック設定が無効の場合は閉じる操作で振動しない`() {
        every { onDismiss() } returns Unit
        show(hapticFeedbackEnabled = false)

        rule.onNodeWithText("閉じる").performClick()

        verify(exactly = 0) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        verify(exactly = 1) { onDismiss() }
        confirmVerified(haptic, onDismiss)
    }

    private fun show(hapticFeedbackEnabled: Boolean = true) {
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                AppTheme {
                    VersionMismatchBottomSheetContent(
                        windowsKoDriverVersion = "2.0.0",
                        appVersion = "1.0.0",
                        hapticFeedbackEnabled = hapticFeedbackEnabled,
                        onDismiss = onDismiss,
                    )
                }
            }
        }
    }
}
