package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import org.junit.Rule
import org.junit.Test

class LmuWindowsReadoutPitTimingDetailPaneSyncTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `全ての文言欄で古い保存結果を無視し最新の保存完了後は外部更新を反映する`() {
        var savedState by mutableStateOf(
            LmuWindowsReadoutPitTimingDetailUiState(
                virtualEnergyText = "初期0",
                virtualEnergyImminentText = "初期1",
                tyreWearText = "初期2",
                tyreWearImminentText = "初期3",
                isTextToSpeechAvailable = true,
            ),
        )
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(uiState = savedState)
            }
        }
        val updates =
            listOf<
                (
                    LmuWindowsReadoutPitTimingDetailUiState,
                    String,
                ) -> LmuWindowsReadoutPitTimingDetailUiState,
            >(
                { state, value -> state.copy(virtualEnergyText = value) },
                { state, value -> state.copy(virtualEnergyImminentText = value) },
                { state, value -> state.copy(tyreWearText = value) },
                { state, value -> state.copy(tyreWearImminentText = value) },
            )
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
            LmuWindowsReadoutPitTimingDetailUiState(
                virtualEnergyText = "初期0",
                virtualEnergyImminentText = "初期1",
                tyreWearText = "初期2",
                tyreWearImminentText = "初期3",
                isTextToSpeechAvailable = true,
            ),
        )
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(uiState = savedState)
            }
        }
        val updates =
            listOf<
                (
                    LmuWindowsReadoutPitTimingDetailUiState,
                    String,
                ) -> LmuWindowsReadoutPitTimingDetailUiState,
            >(
                { state, value -> state.copy(virtualEnergyText = value) },
                { state, value -> state.copy(virtualEnergyImminentText = value) },
                { state, value -> state.copy(tyreWearText = value) },
                { state, value -> state.copy(tyreWearImminentText = value) },
            )
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

    @Test
    fun `全ての文言欄でリセットの保存待ちを保護し完了後は外部更新を反映する`() {
        var savedState by mutableStateOf(
            LmuWindowsReadoutPitTimingDetailUiState(
                virtualEnergyText = "初期0",
                virtualEnergyImminentText = "初期1",
                tyreWearText = "初期2",
                tyreWearImminentText = "初期3",
                isTextToSpeechAvailable = true,
            ),
        )
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(uiState = savedState)
            }
        }
        val updates =
            listOf<
                (
                    LmuWindowsReadoutPitTimingDetailUiState,
                    String,
                ) -> LmuWindowsReadoutPitTimingDetailUiState,
            >(
                { state, value -> state.copy(virtualEnergyText = value) },
                { state, value -> state.copy(virtualEnergyImminentText = value) },
                { state, value -> state.copy(tyreWearText = value) },
                { state, value -> state.copy(tyreWearImminentText = value) },
            )
        val defaults =
            listOf(
                LmuWindowsReadoutPitTimingDetailUiState().virtualEnergyText,
                LmuWindowsReadoutPitTimingDetailUiState().virtualEnergyImminentText,
                LmuWindowsReadoutPitTimingDetailUiState().tyreWearText,
                LmuWindowsReadoutPitTimingDetailUiState().tyreWearImminentText,
            )
        val resetIndices = listOf(0, 1, 3, 4)
        updates.forEachIndexed { index, update ->
            rule
                .onNode(hasSetTextAction() and hasText("初期$index"))
                .performScrollTo()
                .performTextReplacement("変更中$index")
            rule
                .onAllNodesWithContentDescription("デフォルトに戻す")[resetIndices[index]]
                .performScrollTo()
                .performClick()
            rule.runOnIdle { savedState = update(savedState, "変更中$index") }
            rule.waitForIdle()
            // 別の欄と既定文言が同じ場合があるため、対象欄の位置で検証する。
            rule.onAllNodes(hasSetTextAction())[index].assert(hasText(defaults[index]))
            rule.runOnIdle { savedState = update(savedState, defaults[index]) }
            rule.waitForIdle()
            rule.runOnIdle { savedState = update(savedState, "外部更新$index") }
            rule.waitForIdle()
            rule.onNode(hasSetTextAction() and hasText("外部更新$index")).assertExists()
        }
    }
}
