package kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class AceWindowsReadoutVehicleApproachDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文と閾値設定と接近開始時の読み上げカードが表示される`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutVehicleApproachDetailPaneContent()
            }
        }

        rule.onNodeWithText("周囲の車両が接近した際に音声でお知らせします。").assertIsDisplayed()
        rule.onNodeWithText("閾値設定").assertIsDisplayed()
        rule.onNodeWithText("車両間隔: 6.0 m").assertIsDisplayed()
        rule.onNodeWithText("接近開始時の読み上げ").assertIsDisplayed()
        rule.onNodeWithText("車両接近").assertIsDisplayed()
    }

    @Test
    fun `ヘルプアイコンをタップするとヘルプ説明が表示される`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutVehicleApproachDetailPaneContent()
            }
        }

        rule.onNode(hasContentDescription("閾値の説明を表示")).performClick()

        rule
            .onNodeWithText(
                "閾値は自車中心から相手車両中心までの距離です。この範囲内に入ると車両接近と判定され読み上げられます。\n\n" +
                    "※車両は一般的に全長 5 m 以下、全幅 2 m 以下となっています。",
            ).assertIsDisplayed()
    }

    @Test
    fun `閾値スライダーの値を確定するとonThresholdChangedが呼ばれる`() {
        var changedMeters: Double? = null
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutVehicleApproachDetailPaneContent(
                    onThresholdChanged = { changedMeters = it },
                )
            }
        }

        rule
            .onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 6f, range = 2f..10f, steps = 79)),
            ).performSemanticsAction(SemanticsActions.SetProgress) {
                it(7f)
            }

        assertEquals(7.0, changedMeters)
    }

    @Test
    fun `閾値のリセットボタンをタップするとonResetThresholdが呼ばれる`() {
        var resetCalled = false
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = AceWindowsReadoutVehicleApproachDetailUiState(thresholdMeters = 7.0),
                    onResetThreshold = { resetCalled = true },
                )
            }
        }

        rule.onAllNodesWithContentDescription("デフォルトに戻す")[0].performClick()

        assertEquals(true, resetCalled)
    }

    @Test
    fun `カードをタップするとonStartReadoutEnabledChangedが呼ばれる`() {
        var enabled: Boolean? = null
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = AceWindowsReadoutVehicleApproachDetailUiState(startReadoutEnabled = true),
                    onStartReadoutEnabledChanged = { enabled = it },
                )
            }
        }

        rule.onNodeWithText("接近開始時の読み上げ").performClick()

        assertEquals(false, enabled)
    }

    @Test
    fun `試聴ボタンをタップすると入力文言が渡される`() {
        var previewText: String? = null
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = AceWindowsReadoutVehicleApproachDetailUiState(isTextToSpeechAvailable = true),
                    onPreviewClicked = { previewText = it },
                )
            }
        }

        rule.onNodeWithContentDescription("入力した文言を再生").assertIsEnabled().performClick()

        assertEquals("車両接近", previewText)
    }

    @Test
    fun `TTS利用不可なら入力と試聴を無効にして案内する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = AceWindowsReadoutVehicleApproachDetailUiState(startReadoutEnabled = false),
                )
            }
        }

        rule.onNode(hasText("車両接近") and isNotEnabled()).assertExists()
        rule.onNodeWithContentDescription("入力した文言を再生").assertIsNotEnabled()
        rule.onNodeWithText("この端末では音声合成を利用できないため、接近開始時は読み上げません").assertIsDisplayed()
    }

    @Test
    fun `入力を保存し空白では案内を表示してリセットできる`() {
        var savedState by mutableStateOf(AceWindowsReadoutVehicleApproachDetailUiState(isTextToSpeechAvailable = true))
        val changes = mutableListOf<String>()
        var resetCount = 0
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = savedState,
                    onReadoutTextChanged = {
                        changes += it
                        savedState = savedState.copy(readoutText = it.trim())
                    },
                    onReadoutTextReset = { resetCount++ },
                )
            }
        }
        rule.onNode(hasSetTextAction()).performTextReplacement("周囲に注意")
        rule.onNode(hasSetTextAction() and hasText("周囲に注意")).assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement(" ")
        rule.onNodeWithText("空欄のままなら読み上げません").assertIsDisplayed()
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[1].performClick()
        rule.onNode(hasSetTextAction() and hasText("車両接近")).assertExists()
        assertEquals(listOf("周囲に注意", " "), changes)
        assertEquals(1, resetCount)
    }

    @Test
    fun `古い保存結果を無視し最新の保存完了後は外部更新を反映する`() {
        var savedState by mutableStateOf(
            AceWindowsReadoutVehicleApproachDetailUiState(readoutText = "初期", isTextToSpeechAvailable = true),
        )
        rule.setContent {
            MaterialTheme { AceWindowsReadoutVehicleApproachDetailPaneContent(uiState = savedState) }
        }
        rule.onNode(hasSetTextAction()).performTextReplacement("先の入力")
        rule.onNode(hasSetTextAction()).performTextReplacement(" 最新入力 ")
        rule.runOnIdle { savedState = savedState.copy(readoutText = "先の入力") }
        rule.waitForIdle()
        rule.onNode(hasSetTextAction() and hasText(" 最新入力 ")).assertExists()
        rule.runOnIdle { savedState = savedState.copy(readoutText = "最新入力") }
        rule.waitForIdle()
        rule.runOnIdle { savedState = savedState.copy(readoutText = "外部更新") }
        rule.waitForIdle()
        rule.onNode(hasSetTextAction() and hasText("外部更新")).assertExists()
    }

    @Test
    fun `同じ保存値と上限超過入力の後も外部更新を反映する`() {
        var savedState by mutableStateOf(
            AceWindowsReadoutVehicleApproachDetailUiState(readoutText = "初期", isTextToSpeechAvailable = true),
        )
        rule.setContent {
            MaterialTheme { AceWindowsReadoutVehicleApproachDetailPaneContent(uiState = savedState) }
        }
        rule.onNode(hasSetTextAction()).performTextReplacement(" 初期 ")
        rule.waitForIdle()
        rule.runOnIdle { savedState = savedState.copy(readoutText = "境界前") }
        rule.waitForIdle()
        rule.onNode(hasSetTextAction() and hasText("境界前")).assertExists()
        val longText = "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 1)
        rule.onNode(hasSetTextAction()).performTextReplacement(longText)
        rule.runOnIdle { savedState = savedState.copy(readoutText = longText.take(READOUT_CUSTOM_TEXT_MAX_LENGTH)) }
        rule.waitForIdle()
        rule.runOnIdle { savedState = savedState.copy(readoutText = "外部更新") }
        rule.waitForIdle()
        rule.onNode(hasSetTextAction() and hasText("外部更新")).assertExists()
    }

    @Test
    fun `リセットの保存待ちも古い保存結果から保護する`() {
        var savedState by mutableStateOf(
            AceWindowsReadoutVehicleApproachDetailUiState(readoutText = "初期", isTextToSpeechAvailable = true),
        )
        rule.setContent {
            MaterialTheme { AceWindowsReadoutVehicleApproachDetailPaneContent(uiState = savedState) }
        }
        rule.onNode(hasSetTextAction()).performTextReplacement("変更中")
        rule.onAllNodesWithContentDescription("デフォルトに戻す")[1].performClick()
        rule.runOnIdle { savedState = savedState.copy(readoutText = "変更中") }
        rule.waitForIdle()
        rule.onNode(hasSetTextAction() and hasText("車両接近")).assertExists()
        rule.runOnIdle { savedState = savedState.copy(readoutText = "車両接近") }
        rule.waitForIdle()
        rule.runOnIdle { savedState = savedState.copy(readoutText = "外部更新") }
        rule.waitForIdle()
        rule.onNode(hasSetTextAction() and hasText("外部更新")).assertExists()
    }
}
