package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.LmuWindowsBrakeWearInvestigationData
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutBrakeWearDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    private val current =
        LmuWindowsBrakeWearInvestigationData(
            wearablesBrakes = listOf(0.036, 0.035, 0.032, 0.031),
            brakeInfo = listOf(0.041, 0.042, 0.043, 0.044, 0.5),
        )

    @Test
    fun `説明文と2つの取得元のタイトルが表示される`() {
        rule.setContent { KoDriverTheme { LmuWindowsReadoutBrakeWearDetailPaneContent() } }

        rule
            .onNodeWithText(
                "ブレーキ摩耗の調査用に、LMUのREST APIから取得した値をそのまま表示します。" +
                    "ブレーキ交換や周回後に値がどう変わるかを見て、どちらが摩耗を表すかを確認してください。\n" +
                    "デスクトップ版でLMUを起動しているときのみ取得できます。",
            ).assertIsDisplayed()
        rule.onNodeWithText("wearables.brakes").assertIsDisplayed()
        rule.onNodeWithText("brakeinfo").assertIsDisplayed()
    }

    @Test
    fun `値を取得できない場合は両方とも取得できませんと表示する`() {
        rule.setContent { KoDriverTheme { LmuWindowsReadoutBrakeWearDetailPaneContent() } }

        rule.onAllNodesWithText("取得できません").assertCountEquals(2)
    }

    @Test
    fun `4輪の値は輪ごとのラベルで表示し5要素目以降は番号で表示する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent(
                    uiState = LmuWindowsReadoutBrakeWearDetailUiState(current = current),
                )
            }
        }

        rule.onNodeWithText("FL: 0.03600000").assertIsDisplayed()
        rule.onNodeWithText("FR: 0.03500000").assertIsDisplayed()
        rule.onNodeWithText("RL: 0.03200000").assertIsDisplayed()
        rule.onNodeWithText("RR: 0.03100000").assertIsDisplayed()
        rule.onNodeWithText("FL: 0.04100000").assertIsDisplayed()
        rule.onNodeWithText("#5: 0.50000000").assertIsDisplayed()
    }

    @Test
    fun `基準が設定されていると基準との差を表示する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutBrakeWearDetailUiState(
                            current = LmuWindowsBrakeWearInvestigationData(wearablesBrakes = listOf(0.034)),
                            baseline = LmuWindowsBrakeWearInvestigationData(wearablesBrakes = listOf(0.036)),
                        ),
                )
            }
        }

        rule.onNodeWithText("FL: 0.03400000（基準との差 -0.00200000）").assertIsDisplayed()
    }

    @Test
    fun `基準側に対応する要素がない場合は差を表示しない`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutBrakeWearDetailUiState(
                            current = LmuWindowsBrakeWearInvestigationData(wearablesBrakes = listOf(0.034, 0.033)),
                            baseline = LmuWindowsBrakeWearInvestigationData(wearablesBrakes = listOf(0.036)),
                        ),
                )
            }
        }

        rule.onNodeWithText("FR: 0.03300000").assertIsDisplayed()
    }

    @Test
    fun `基準未設定ではクリアボタンが無効で設定済みなら有効になる`() {
        var state by mutableStateOf(LmuWindowsReadoutBrakeWearDetailUiState())
        rule.setContent {
            KoDriverTheme { LmuWindowsReadoutBrakeWearDetailPaneContent(uiState = state) }
        }
        rule.onNodeWithText("基準をクリア").assertIsNotEnabled()

        state = LmuWindowsReadoutBrakeWearDetailUiState(baseline = LmuWindowsBrakeWearInvestigationData())

        rule.onNodeWithText("基準をクリア").assertIsEnabled()
    }

    @Test
    fun `ボタン押下でコールバックが呼ばれる`() {
        var setCount = 0
        var clearedCount = 0
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutBrakeWearDetailUiState(baseline = LmuWindowsBrakeWearInvestigationData()),
                    onBaselineSet = { setCount++ },
                    onBaselineCleared = { clearedCount++ },
                )
            }
        }

        rule.onNodeWithText("現在値を基準にする").performClick()
        rule.onNodeWithText("基準をクリア").performClick()

        assertEquals(1, setCount)
        assertEquals(1, clearedCount)
    }
}
