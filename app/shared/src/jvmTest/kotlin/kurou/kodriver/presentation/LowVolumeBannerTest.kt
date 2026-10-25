package kurou.kodriver.presentation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.feature.main.LowVolumeWarningUiState
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

    @Test
    fun `閉じる領域の右に本文があり本文の右にシェブロンがある`() {
        show(LowVolumeWarningUiState(deviceVolume = 20, soundVolume = 30))

        val closeBounds = unmergedBounds(rule.onNodeWithContentDescription("音量警告を閉じる", useUnmergedTree = true))
        val messageBounds = unmergedBounds(rule.onNodeWithTag("lowVolumeBannerMessage", useUnmergedTree = true))
        val chevronBounds = unmergedBounds(rule.onNodeWithTag("lowVolumeBannerChevron", useUnmergedTree = true))

        assertTrue(closeBounds.right < messageBounds.left)
        assertTrue(messageBounds.right < chevronBounds.left)
    }

    @Test
    fun `文字サイズを拡大するとバナーの高さが文言に追従して伸びる`() {
        val uiState = LowVolumeWarningUiState(deviceVolume = 20, soundVolume = 30)
        var fontScale by mutableFloatStateOf(1f)
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                AppTheme {
                    LowVolumeBanner(
                        LowVolumeWarningUiState(deviceVolume = 20, soundVolume = 30),
                        onClick,
                        onDismiss,
                    )
                }
            }
        }
        val normalHeight =
            rule
                .onRoot()
                .fetchSemanticsNode()
                .size.height

        fontScale = 2f
        rule.waitForIdle()

        assertTrue(
            rule
                .onRoot()
                .fetchSemanticsNode()
                .size.height > normalHeight,
        )
    }

    private fun unmergedBounds(node: SemanticsNodeInteraction) = node.fetchSemanticsNode().boundsInRoot

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
