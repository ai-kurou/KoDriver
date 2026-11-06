package kurou.kodriver.feature.main

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
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
                SpeechRecognitionCard(
                    uiState = SpeechRecognitionUiState(),
                    onEnabledChange = { changes += it },
                    onDeviceMenuOpen = {},
                    onDeviceSelected = {},
                )
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
                    onDeviceMenuOpen = {},
                    onDeviceSelected = {},
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
    fun `入力音量があるときだけ音量のゲージを表示する`() {
        val level = mutableStateOf<Float?>(null)
        rule.setContent {
            KoDriverTheme {
                SpeechRecognitionCard(
                    uiState =
                        SpeechRecognitionUiState(
                            enabled = true,
                            status = SpeechRecognitionStatus.Listening,
                            level = level.value,
                        ),
                    onEnabledChange = {},
                    onDeviceMenuOpen = {},
                    onDeviceSelected = {},
                )
            }
        }

        rule.onNodeWithContentDescription("マイクの入力音量").assertDoesNotExist()
        level.value = 0.4f
        rule.onNodeWithContentDescription("マイクの入力音量").assertExists()
        rule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(0.4f, 0f..1f))).assertIsDisplayed()
    }

    @Test
    fun `状態ごとの案内文を表示する`() {
        val status = mutableStateOf(SpeechRecognitionStatus.Stopped)
        rule.setContent {
            KoDriverTheme {
                SpeechRecognitionCard(
                    uiState = SpeechRecognitionUiState(status = status.value),
                    onEnabledChange = {},
                    onDeviceMenuOpen = {},
                    onDeviceSelected = {},
                )
            }
        }
        mapOf(
            SpeechRecognitionStatus.Stopped to "停止中",
            SpeechRecognitionStatus.Starting to "準備中(マイクを使用中)",
            SpeechRecognitionStatus.Listening to "聞き取り中(マイクを使用中)",
            SpeechRecognitionStatus.PermissionDenied to "マイクの権限が許可されていません",
            SpeechRecognitionStatus.Unavailable to "この端末では音声認識を利用できません",
            SpeechRecognitionStatus.MicrophoneUnavailable to "マイクを開けないか、使えなくなりました",
            SpeechRecognitionStatus.NoSignal to "マイクの音声が届いていません。OSのマイク設定を確認してください",
            SpeechRecognitionStatus.RecognitionFailed to "音声認識中にエラーが発生しました",
            SpeechRecognitionStatus.IdleStopped to "しばらく発話がなかったため停止しました",
        ).forEach { (expectedStatus, text) ->
            status.value = expectedStatus
            rule.onNodeWithText(text).assertExists()
        }
    }

    @Test
    fun `入力デバイスの一覧があるときは選択中のデバイスを表示しメニューを開くと通知し選ぶと通知する`() {
        var menuOpenCount = 0
        val selected = mutableListOf<String>()
        rule.setContent {
            KoDriverTheme {
                SpeechRecognitionCard(
                    uiState = SpeechRecognitionUiState(deviceName = "Mic A", devices = listOf("Mic A", "Mic B")),
                    onEnabledChange = {},
                    onDeviceMenuOpen = { menuOpenCount++ },
                    onDeviceSelected = { selected += it },
                )
            }
        }

        rule.onNodeWithTag("speech_recognition_device").assertTextContains("Mic A")
        rule.onNodeWithTag("speech_recognition_device").performClick()
        rule.onNodeWithTag("speech_recognition_device_Mic B").performClick()

        rule.runOnIdle {
            assertEquals(1, menuOpenCount)
            assertEquals(listOf("Mic B"), selected)
        }
        rule.onNodeWithTag("speech_recognition_device_Mic B").assertDoesNotExist()
    }

    @Test
    fun `入力デバイスの一覧が空か選択中のデバイスが無いときはデバイスの選択を表示しない`() {
        val uiState = mutableStateOf(SpeechRecognitionUiState(devices = listOf("Mic A")))
        rule.setContent {
            KoDriverTheme {
                SpeechRecognitionCard(
                    uiState = uiState.value,
                    onEnabledChange = {},
                    onDeviceMenuOpen = {},
                    onDeviceSelected = {},
                )
            }
        }

        rule.onNodeWithTag("speech_recognition_device").assertDoesNotExist()
        uiState.value = SpeechRecognitionUiState(deviceName = "Mic A")
        rule.onNodeWithTag("speech_recognition_device").assertDoesNotExist()
    }
}
