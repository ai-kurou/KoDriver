package kurou.kodriver.feature.main

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

class MicrophoneInputCardTest {
    @get:Rule
    val rule = createComposeRule()

    private fun setCard(
        uiState: MicrophoneInputUiState,
        changes: MutableList<Boolean> = mutableListOf(),
    ) {
        rule.setContent {
            KoDriverTheme { MicrophoneInputCard(uiState = uiState, onEnabledChange = { changes += it }) }
        }
    }

    @Test
    fun `停止中はOFFのスイッチと停止中を表示し操作でONを通知する`() {
        val changes = mutableListOf<Boolean>()
        setCard(MicrophoneInputUiState(), changes)

        rule.onNodeWithText("マイク入力").assertExists()
        rule.onNodeWithText("停止中").assertExists()
        rule.onNodeWithText("入力デバイスが見つかりません").assertDoesNotExist()
        rule.onNodeWithContentDescription("マイクの入力音量").assertDoesNotExist()
        rule.onNodeWithContentDescription("マイク入力の確認を切り替え").assertIsOff().performClick()
        rule.runOnIdle { assertEquals(listOf(true), changes) }
    }

    @Test
    fun `入力中はデバイス名と音量のメーターを表示し操作でOFFを通知する`() {
        val changes = mutableListOf<Boolean>()
        setCard(
            MicrophoneInputUiState(
                enabled = true,
                status = MicrophoneInputStatus.Capturing,
                deviceName = "Mic A",
                level = 0.5f,
            ),
            changes,
        )

        rule.onNodeWithText("入力中(マイクを使用中)").assertExists()
        rule.onNodeWithText("入力デバイス: Mic A").assertExists()
        rule.onNodeWithContentDescription("マイクの入力音量").assertExists()
        rule.onNodeWithContentDescription("マイク入力の確認を切り替え").assertIsOn().performClick()
        rule.runOnIdle { assertEquals(listOf(false), changes) }
    }

    @Test
    fun `準備中でデバイス名が未取得のときはデバイスの行を表示しない`() {
        setCard(MicrophoneInputUiState(enabled = true, status = MicrophoneInputStatus.Starting))

        rule.onNodeWithText("準備中(マイクを使用中)").assertExists()
        rule.onNodeWithText("入力デバイスが見つかりません").assertDoesNotExist()
        rule.onNodeWithContentDescription("マイクの入力音量").assertDoesNotExist()
    }

    @Test
    fun `デバイス名が無いまま入力中や失敗のときはデバイスが見つからない表示になる`() {
        setCard(MicrophoneInputUiState(status = MicrophoneInputStatus.Unavailable))

        rule.onNodeWithText("入力デバイスが見つかりません").assertExists()
    }

    @Test
    fun `無音のときはプライバシー設定の案内を表示する`() {
        setCard(MicrophoneInputUiState(status = MicrophoneInputStatus.NoSignal, deviceName = "Mic A"))

        rule.onNodeWithText("音声が入力されていません").assertExists()
        val guidance = "Windows の設定 > プライバシーとセキュリティ > マイク で、マイクへのアクセスが許可されているか確認してください。"
        rule.onNodeWithText(guidance).assertExists()
    }

    @Test
    fun `利用できないときはその旨を表示し案内は出さない`() {
        setCard(MicrophoneInputUiState(status = MicrophoneInputStatus.Unavailable))

        rule.onNodeWithText("マイクを利用できません").assertExists()
        rule.onNodeWithText("音声が入力されていません").assertDoesNotExist()
    }
}
