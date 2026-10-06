package kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail

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

class LmuWindowsReadoutRemainingVirtualEnergyDetailPaneSyncTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `全ての文言欄で古い保存結果を無視し最新の保存完了後は外部更新を反映する`() {
        var savedState by mutableStateOf(
            LmuWindowsReadoutRemainingVirtualEnergyDetailUiState(
                readoutText = "初期0",
                isTextToSpeechAvailable = true,
            ),
        )
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent(uiState = savedState)
            }
        }
        val updates =
            listOf<
                (
                    LmuWindowsReadoutRemainingVirtualEnergyDetailUiState,
                    String,
                ) -> LmuWindowsReadoutRemainingVirtualEnergyDetailUiState,
            >({ state, value -> state.copy(readoutText = value) })
        updates.forEachIndexed { index, update ->
            rule
                .onNode(hasSetTextAction() and hasText("初期$index"))
                .performScrollTo()
                .performTextReplacement("先の入力")
            rule
                .onNode(hasSetTextAction() and hasText("先の入力"))
                .performTextReplacement(" 最新入力 ")
            rule.runOnIdle { savedState = update(savedState, "先の入力") }
            rule.waitForIdle()
            rule.onNode(hasSetTextAction() and hasText(" 最新入力 ")).assertExists()
            rule.runOnIdle { savedState = update(savedState, "最新入力") }
            rule.waitForIdle()
            rule.runOnIdle { savedState = update(savedState, "外部更新$index") }
            rule.waitForIdle()
            rule.onNode(hasSetTextAction() and hasText("外部更新$index")).assertExists()
        }
    }

    @Test
    fun `全ての文言欄で保存値が変わらない入力と上限超過入力の後も外部更新を反映する`() {
        var savedState by mutableStateOf(
            LmuWindowsReadoutRemainingVirtualEnergyDetailUiState(
                readoutText = "初期0",
                isTextToSpeechAvailable = true,
            ),
        )
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutRemainingVirtualEnergyDetailPaneContent(uiState = savedState)
            }
        }
        val updates =
            listOf<
                (
                    LmuWindowsReadoutRemainingVirtualEnergyDetailUiState,
                    String,
                ) -> LmuWindowsReadoutRemainingVirtualEnergyDetailUiState,
            >({ state, value -> state.copy(readoutText = value) })
        updates.forEachIndexed { index, update ->
            rule
                .onNode(hasSetTextAction() and hasText("初期$index"))
                .performScrollTo()
                .performTextReplacement(" 初期$index ")
            // 正規化後の値は保存済みと同じなので、保存値の再通知なしで待機を解除する。
            rule.waitForIdle()
            rule.runOnIdle { savedState = update(savedState, "境界前$index") }
            rule.waitForIdle()
            rule.onNode(hasSetTextAction() and hasText("境界前$index")).assertExists()
            val longText = "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 1)
            rule
                .onNode(hasSetTextAction() and hasText("境界前$index"))
                .performTextReplacement(longText)
            rule.runOnIdle { savedState = update(savedState, longText.take(READOUT_CUSTOM_TEXT_MAX_LENGTH)) }
            rule.waitForIdle()
            rule.runOnIdle { savedState = update(savedState, "外部更新$index") }
            rule.waitForIdle()
            rule.onNode(hasSetTextAction() and hasText("外部更新$index")).assertExists()
        }
    }
}
