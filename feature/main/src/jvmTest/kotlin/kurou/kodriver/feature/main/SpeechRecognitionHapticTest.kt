package kurou.kodriver.feature.main

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class SpeechRecognitionHapticTest {
    @get:Rule
    val rule = createComposeRule()

    private val feedback = mutableListOf<HapticFeedbackType>()
    private val haptic =
        object : HapticFeedback {
            override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
                feedback += hapticFeedbackType
            }
        }

    private fun setContent(content: @androidx.compose.runtime.Composable () -> Unit) {
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) { KoDriverTheme { content() } }
        }
    }

    @Test
    fun `チップをタップするとハプティックとクリックが呼ばれる`() {
        var clicks = 0
        setContent {
            SpeechRecognitionChip(uiState = SpeechRecognitionUiState(), onClick = { clicks++ })
        }

        rule.onNodeWithContentDescription("停止中").performClick()

        rule.runOnIdle {
            assertEquals(1, clicks)
            assertEquals(listOf(HapticFeedbackType.ContextClick), feedback)
        }
    }

    @Test
    fun `スイッチを切り替えるとハプティックと通知が呼ばれる`() {
        val changes = mutableListOf<Boolean>()
        setContent {
            SpeechRecognitionDetailContent(
                uiState = SpeechRecognitionUiState(),
                onEnabledChange = { changes += it },
                onDeviceMenuOpen = {},
                onDeviceSelected = {},
            )
        }

        rule.onNodeWithContentDescription("音声認識の聞き取りを切り替え").performClick()

        rule.runOnIdle {
            assertEquals(listOf(true), changes)
            assertEquals(listOf(HapticFeedbackType.ContextClick), feedback)
        }
    }

    @Test
    fun `入力デバイスを選ぶとハプティックと選択通知が呼ばれる`() {
        val selected = mutableListOf<String>()
        setContent {
            AudioInputDeviceDropdown(
                devices = listOf("Mic A", "Mic B"),
                selected = "Mic A",
                onMenuOpen = {},
                onSelected = { selected += it },
            )
        }

        rule.onNodeWithTag("speech_recognition_device").performClick()
        rule.onNodeWithTag("speech_recognition_device_Mic B").performClick()

        rule.runOnIdle {
            assertEquals(listOf("Mic B"), selected)
            assertEquals(listOf(HapticFeedbackType.ContextClick), feedback)
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `ボトムシートを閉じるとハプティックと閉じる通知が呼ばれる`() {
        var dismissed = 0
        setContent {
            SpeechRecognitionDetail(
                presentation = SpeechRecognitionDetailPresentation.BottomSheet,
                uiState = SpeechRecognitionUiState(),
                onEnabledChange = {},
                onDeviceMenuOpen = {},
                onDeviceSelected = {},
                onDismissRequest = { dismissed++ },
            )
        }

        rule.onAllNodes(isRoot())[1].performMouseInput { click(Offset(1f, 1f)) }

        rule.runOnIdle {
            assertEquals(1, dismissed)
            assertEquals(listOf(HapticFeedbackType.ContextClick), feedback)
        }
    }
}
