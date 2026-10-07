package kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import org.junit.Rule
import org.junit.Test

class AceWindowsReadoutTyreTemperatureDetailPaneSyncTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `過熱文言欄で古い保存結果を無視し最新の保存完了後は外部更新を反映する`() {
        var savedState by mutableStateOf(
            AceWindowsReadoutTyreTemperatureDetailUiState(
                overheatReadoutText = "初期",
                isTextToSpeechAvailable = true,
            ),
        )
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent(uiState = savedState)
            }
        }
        rule
            .onNode(hasSetTextAction() and hasText("初期"))
            .performScrollTo()
            .performTextReplacement("先の入力")
        rule
            .onNode(hasSetTextAction() and hasText("先の入力"))
            .performTextReplacement(" 最新入力 ")
        rule.runOnIdle { savedState = savedState.copy(overheatReadoutText = "先の入力") }
        rule.waitForIdle()
        rule.onNode(hasSetTextAction() and hasText(" 最新入力 ")).assertExists()
        rule.runOnIdle { savedState = savedState.copy(overheatReadoutText = "最新入力") }
        rule.waitForIdle()
        rule.runOnIdle { savedState = savedState.copy(overheatReadoutText = "外部更新") }
        rule.waitForIdle()
        rule.onNode(hasSetTextAction() and hasText("外部更新")).assertExists()
    }

    @Test
    fun `過熱文言欄で保存値が変わらない入力と上限超過入力の後も外部更新を反映する`() {
        var savedState by mutableStateOf(
            AceWindowsReadoutTyreTemperatureDetailUiState(
                overheatReadoutText = "初期",
                isTextToSpeechAvailable = true,
            ),
        )
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutTyreTemperatureDetailPaneContent(uiState = savedState)
            }
        }
        rule
            .onNode(hasSetTextAction() and hasText("初期"))
            .performScrollTo()
            .performTextReplacement(" 初期 ")
        // 正規化後の値は保存済みと同じなので、保存値の再通知なしで待機を解除する。
        rule.waitForIdle()
        rule.runOnIdle { savedState = savedState.copy(overheatReadoutText = "境界前") }
        rule.waitForIdle()
        rule.onNode(hasSetTextAction() and hasText("境界前")).assertExists()
        val longText = "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 1)
        rule
            .onNode(hasSetTextAction() and hasText("境界前"))
            .performTextReplacement(longText)
        rule.runOnIdle {
            savedState = savedState.copy(overheatReadoutText = longText.take(READOUT_CUSTOM_TEXT_MAX_LENGTH))
        }
        rule.waitForIdle()
        rule.runOnIdle { savedState = savedState.copy(overheatReadoutText = "外部更新") }
        rule.waitForIdle()
        rule.onNode(hasSetTextAction() and hasText("外部更新")).assertExists()
    }
}
