package kurou.kodriver.feature.othervolumedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherVolumeDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    private val haptic: HapticFeedback = mockk()

    private val hasSliderProgressBarRangeInfo =
        SemanticsMatcher("ProgressBarRangeInfoを持つスライダー") {
            it.config.contains(SemanticsProperties.ProgressBarRangeInfo)
        }

    @Test
    fun `音量スライダーをドラッグしても振動しない`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                MaterialTheme {
                    OtherVolumeDetailPaneContent(
                        uiState = OtherVolumeDetailUiState(volume = 80, deviceVolume = 60),
                    )
                }
            }
        }

        rule.onAllNodes(hasSliderProgressBarRangeInfo)[0].performTouchInput { swipeLeft() }
        rule.onAllNodes(hasSliderProgressBarRangeInfo)[1].performTouchInput { swipeLeft() }

        verify(exactly = 0) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `音量スライダー操作を完了すると変更後の音量を通知する`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        var changedVolume: Int? = null
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                MaterialTheme {
                    OtherVolumeDetailPaneContent(
                        uiState = OtherVolumeDetailUiState(volume = 80, deviceVolume = 60),
                        onVolumeChanged = { changedVolume = it },
                    )
                }
            }
        }

        rule
            .onAllNodes(hasSliderProgressBarRangeInfo)[0]
            .performSemanticsAction(SemanticsActions.SetProgress) { it(50f) }

        assertEquals(50, changedVolume)
        verify(exactly = 0) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `端末のマスター音量スライダー操作を完了すると変更後の音量を通知する`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        var changedDeviceVolume: Int? = null
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                MaterialTheme {
                    OtherVolumeDetailPaneContent(
                        uiState = OtherVolumeDetailUiState(volume = 80, deviceVolume = 60),
                        onDeviceVolumeChanged = { changedDeviceVolume = it },
                    )
                }
            }
        }

        rule
            .onAllNodes(hasSliderProgressBarRangeInfo)[1]
            .performSemanticsAction(SemanticsActions.SetProgress) { it(30f) }

        assertEquals(30, changedDeviceVolume)
        verify(exactly = 0) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `試聴ボタンをタップするとonPreviewClickedが呼ばれる`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        var previewCount = 0
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                MaterialTheme {
                    OtherVolumeDetailPaneContent(
                        uiState = OtherVolumeDetailUiState(volume = 80, deviceVolume = 60),
                        onPreviewClicked = { previewCount++ },
                    )
                }
            }
        }

        rule.onNode(hasText("試聴")).performClick()

        assertEquals(1, previewCount)
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `試聴中は停止ボタンを表示しタップを通知する`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        var stopCount = 0
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                MaterialTheme {
                    OtherVolumeDetailPaneContent(
                        uiState = OtherVolumeDetailUiState(isPreviewing = true),
                        onPreviewClicked = { stopCount++ },
                    )
                }
            }
        }
        rule.onNode(hasText("停止")).performClick()
        rule.onNode(hasText("試聴")).assertDoesNotExist()
        assertEquals(1, stopCount)
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `戻るボタンをタップするとonBackが呼ばれる`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        var backCount = 0
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                MaterialTheme {
                    OtherVolumeDetailPaneContent(
                        uiState = OtherVolumeDetailUiState(lowVolumeWarningEnabled = true),
                        onBack = { backCount++ },
                    )
                }
            }
        }

        rule.onNode(hasContentDescription("戻る")).performClick()

        assertEquals(1, backCount)
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `警告スイッチを表示しクリックを通知する`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        var enabled by mutableStateOf(true)
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                MaterialTheme {
                    OtherVolumeDetailPaneContent(
                        uiState = OtherVolumeDetailUiState(lowVolumeWarningEnabled = enabled),
                        onLowVolumeWarningEnabledChanged = { enabled = it },
                    )
                }
            }
        }
        rule.onNodeWithText("音量低下の警告").performClick()
        assertEquals(false, enabled)
        rule.onNodeWithText("音量低下の警告").performClick()
        assertEquals(true, enabled)
        verify(exactly = 2) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }
}
