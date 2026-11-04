package kurou.kodriver.feature.main

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class SpeechRecognitionCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `停止中はOFFのスイッチと停止中を表示し操作でONを通知する`() {
        val changes = mutableListOf<Boolean>()
        rule.setContent {
            KoDriverTheme {
                SpeechRecognitionCard(uiState = SpeechRecognitionUiState(), onEnabledChange = { changes += it })
            }
        }

        rule.onNodeWithText("音声認識").assertExists()
        rule.onNodeWithText("停止中").assertExists()
        rule.onNodeWithContentDescription("音声認識の聞き取りを切り替え").assertIsOff().performClick()
        rule.runOnIdle { assertEquals(listOf(true), changes) }
    }

    @Test
    fun `聞き取り中はONのスイッチと認識途中の文字列と結果の履歴を表示し操作でOFFを通知する`() {
        val changes = mutableListOf<Boolean>()
        rule.setContent {
            KoDriverTheme {
                SpeechRecognitionCard(
                    uiState =
                        SpeechRecognitionUiState(
                            enabled = true,
                            status = SpeechRecognitionStatus.Listening,
                            partialText = "タイ",
                            recognized = listOf(listOf("燃料", "然料"), listOf("ブレーキ")),
                        ),
                    onEnabledChange = { changes += it },
                )
            }
        }

        rule.onNodeWithText("聞き取り中(マイクを使用中)").assertExists()
        rule.onNodeWithText("タイ").assertExists()
        rule.onNodeWithText("燃料 / 然料").assertExists()
        rule.onNodeWithText("ブレーキ").assertExists()
        rule.onNodeWithContentDescription("音声認識の聞き取りを切り替え").assertIsOn().performClick()
        rule.runOnIdle { assertEquals(listOf(false), changes) }
    }

    @Test
    fun `状態ごとの案内文を表示する`() {
        val status = mutableStateOf(SpeechRecognitionStatus.Stopped)
        rule.setContent {
            KoDriverTheme {
                SpeechRecognitionCard(
                    uiState = SpeechRecognitionUiState(status = status.value),
                    onEnabledChange = {},
                )
            }
        }
        mapOf(
            SpeechRecognitionStatus.Stopped to "停止中",
            SpeechRecognitionStatus.Starting to "準備中(マイクを使用中)",
            SpeechRecognitionStatus.Listening to "聞き取り中(マイクを使用中)",
            SpeechRecognitionStatus.PermissionDenied to "マイクの権限が許可されていません",
            SpeechRecognitionStatus.Unavailable to "この端末では音声認識を利用できません",
            SpeechRecognitionStatus.IdleStopped to "しばらく発話がなかったため停止しました",
        ).forEach { (expectedStatus, text) ->
            status.value = expectedStatus
            rule.onNodeWithText(text).assertExists()
        }
    }
}
