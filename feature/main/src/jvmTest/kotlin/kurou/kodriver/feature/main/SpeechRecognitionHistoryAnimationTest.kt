package kurou.kodriver.feature.main

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test

class SpeechRecognitionHistoryAnimationTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `認識履歴は消える時にフェードしながら消え終わるまで残る`() {
        val uiState = mutableStateOf(SpeechRecognitionUiState(enabled = true, recognized = listOf(listOf("燃料"))))
        showCard(uiState)
        rule.waitForIdle()
        rule.onNodeWithText("燃料").assertIsDisplayed()

        rule.mainClock.autoAdvance = false
        uiState.value = uiState.value.copy(recognized = emptyList())
        rule.mainClock.advanceTimeBy(100)
        rule.onNodeWithText("燃料").assertExists()
        rule.mainClock.advanceTimeBy(400)
        rule.onNodeWithText("燃料").assertDoesNotExist()
    }

    @Test
    fun `認識途中の文字列は消える時にも退場中は残り空表示にならない`() {
        val uiState = mutableStateOf(SpeechRecognitionUiState(enabled = true, partialText = "タイ"))
        showCard(uiState)
        rule.waitForIdle()
        rule.onNodeWithText("タイ").assertIsDisplayed()

        rule.mainClock.autoAdvance = false
        uiState.value = uiState.value.copy(partialText = "")
        rule.mainClock.advanceTimeBy(100)
        rule.onNodeWithText("タイ").assertExists()
        rule.mainClock.advanceTimeBy(400)
        rule.onNodeWithText("タイ").assertDoesNotExist()
    }

    @Test
    fun `認識履歴は現れる時に展開しながら表示される`() {
        val uiState = mutableStateOf(SpeechRecognitionUiState(enabled = true))
        showCard(uiState)
        rule.waitForIdle()
        rule.onNodeWithText("ブレーキ").assertDoesNotExist()

        rule.mainClock.autoAdvance = false
        uiState.value = uiState.value.copy(recognized = listOf(listOf("ブレーキ")))
        rule.mainClock.advanceTimeBy(100)
        rule.onNodeWithText("ブレーキ").assertExists()
        rule.mainClock.advanceTimeBy(400)
        rule.onNodeWithText("ブレーキ").assertIsDisplayed()
    }

    private fun showCard(uiState: MutableState<SpeechRecognitionUiState>) {
        rule.setContent {
            KoDriverTheme {
                SpeechRecognitionCard(
                    uiState = uiState.value,
                    onDeviceMenuOpen = {},
                    onDeviceSelected = {},
                )
            }
        }
    }
}
