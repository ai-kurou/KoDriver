package kurou.kodriver.presentation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.feature.main.LowVolumeWarningUiState
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class LowVolumeBannerTest {
    @get:Rule
    val rule = createComposeRule()

    private val onClick: () -> Unit = mockk()
    private val onDismiss: () -> Unit = mockk()

    @Test
    fun `端末音量だけの警告文言を表示する`() {
        show(LowVolumeWarningUiState(deviceVolume = 20))
        rule.onNodeWithText("端末の音量が低くなっています").assertIsDisplayed()
        rule.onNodeWithText("KoDriverの音量が低くなっています").assertDoesNotExist()
    }

    @Test
    fun `KoDriver音量だけの警告文言を表示する`() {
        show(LowVolumeWarningUiState(soundVolume = 20))
        rule.onNodeWithText("KoDriverの音量が低くなっています").assertIsDisplayed()
        rule.onNodeWithText("端末の音量が低くなっています").assertDoesNotExist()
    }

    @Test
    fun `両方の警告文言を表示する`() {
        show(LowVolumeWarningUiState(deviceVolume = 20, soundVolume = 30))
        rule.onNodeWithText("端末とKoDriverの音量が低くなっています").assertIsDisplayed()
    }

    @Test
    fun `バナー本文をタップするとクリックとハプティックを実行する`() {
        every { onClick() } returns Unit
        val feedback = mutableListOf<HapticFeedbackType>()
        val haptic = recordingHaptic(feedback)
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                AppTheme {
                    LowVolumeBanner(LowVolumeWarningUiState(deviceVolume = 20), onClick, onDismiss)
                }
            }
        }

        rule.onNodeWithText("端末の音量が低くなっています").performClick()

        verify(exactly = 1) { onClick() }
        confirmVerified(onClick, onDismiss)
        assertEquals(listOf(HapticFeedbackType.ContextClick), feedback)
    }

    @Test
    fun `閉じるボタンをタップするとdismissだけを実行する`() {
        every { onDismiss() } returns Unit
        show(LowVolumeWarningUiState(soundVolume = 20))

        rule.onNodeWithContentDescription("音量警告を閉じる").performClick()

        verify(exactly = 1) { onDismiss() }
        confirmVerified(onClick, onDismiss)
    }

    private fun show(uiState: LowVolumeWarningUiState) {
        rule.setContent {
            AppTheme { LowVolumeBanner(uiState, onClick, onDismiss) }
        }
    }

    private fun recordingHaptic(feedback: MutableList<HapticFeedbackType>) =
        object : HapticFeedback {
            override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
                feedback.add(hapticFeedbackType)
            }
        }
}
