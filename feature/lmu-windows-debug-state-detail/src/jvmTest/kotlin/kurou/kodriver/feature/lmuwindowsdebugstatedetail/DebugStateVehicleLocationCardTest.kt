package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.LmuWindowsPitState
import kurou.kodriver.domain.model.LmuWindowsPitStatusData
import org.junit.Rule
import org.junit.Test

class DebugStateVehicleLocationCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `ピット状態が未取得の場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.VEHICLE_LOCATION),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("車両位置").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `ピットレーン走行中とピット状態とガレージ内を表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.VEHICLE_LOCATION),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    pitStatus =
                                        LmuWindowsPitStatusData(
                                            inPits = true,
                                            pitState = LmuWindowsPitState.ENTERING,
                                            inGarageStall = false,
                                        ),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("車両位置").assertIsDisplayed()
        rule.onNodeWithText("ピットレーン走行中: はい").assertIsDisplayed()
        rule.onNodeWithText("ピット状態: Entering（ピットイン中）").assertIsDisplayed()
        rule.onNodeWithText("ガレージ内: いいえ").assertIsDisplayed()
    }

    @Test
    fun `pitStateの各値に対応する表示文言を表示する`() {
        val expectedByPitState =
            mapOf(
                LmuWindowsPitState.NONE to "None（ピット動作なし）",
                LmuWindowsPitState.REQUESTED to "Requested（ピット要求中）",
                LmuWindowsPitState.ENTERING to "Entering（ピットイン中）",
                LmuWindowsPitState.STOPPED to "Stopped（ピット停止中）",
                LmuWindowsPitState.EXITING to "Exiting（ピットアウト中）",
                LmuWindowsPitState.UNKNOWN to "不明",
            )

        expectedByPitState.forEach { (pitState, expectedText) ->
            rule.setContent {
                MaterialTheme {
                    LmuWindowsDebugStateDetailPaneContent(
                        uiState =
                            LmuWindowsDebugStateDetailUiState(
                                cardOrder = listOf(LmuWindowsDebugStateCardKey.VEHICLE_LOCATION),
                                lmuWindows =
                                    LmuWindowsDebugState(
                                        pitStatus =
                                            LmuWindowsPitStatusData(
                                                inPits = false,
                                                pitState = pitState,
                                                inGarageStall = true,
                                            ),
                                    ),
                            ),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            rule.onNodeWithText("車両位置").assertIsDisplayed()
            rule.onNodeWithText("ピット状態: $expectedText").assertIsDisplayed()
            rule.onNodeWithText("ピットレーン走行中: いいえ").assertIsDisplayed()
            rule.onNodeWithText("ガレージ内: はい").assertIsDisplayed()
        }
    }
}
