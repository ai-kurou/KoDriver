package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.core.model.AceWindowsCarLocation
import kurou.kodriver.core.model.AceWindowsStatusData
import kurou.kodriver.core.model.AceWindowsStatusType
import kurou.kodriver.core.model.DebugStateCardKey
import kurou.kodriver.core.model.LmuWindowsPitState
import kurou.kodriver.core.model.LmuWindowsPitStatusData
import kurou.kodriver.core.model.Simulator
import org.junit.Rule
import org.junit.Test

class DebugStateVehicleLocationCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `selectedSimulatorが未選択の場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = null,
                            cardOrder = listOf(DebugStateCardKey.VEHICLE_LOCATION),
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
    fun `selectedSimulatorがACEでステータス情報が未取得の場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.AceWindows,
                            cardOrder = listOf(DebugStateCardKey.VEHICLE_LOCATION),
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
    fun `selectedSimulatorがLMUでピット状態が未取得の場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            cardOrder = listOf(DebugStateCardKey.VEHICLE_LOCATION),
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
    fun `selectedSimulatorがGT7の場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.Gt7Ps5,
                            cardOrder = listOf(DebugStateCardKey.VEHICLE_LOCATION),
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
    fun `selectedSimulatorがACEの場合はstatusとcarLocationの各値に対応する表示文言を表示する`() {
        val expectedStatuses =
            listOf(
                AceWindowsStatusType.OFF to "Off（未起動）",
                AceWindowsStatusType.REPLAY to "Replay（リプレイ中）",
                AceWindowsStatusType.LIVE to "Live（走行中）",
                AceWindowsStatusType.PAUSE to "Pause（ポーズ中）",
                AceWindowsStatusType.UNKNOWN to "不明",
            )
        val expectedCarLocations =
            listOf(
                AceWindowsCarLocation.UNASSIGNED to "Unassigned（未割当）",
                AceWindowsCarLocation.PITLANE to "PitLane（ピットレーン）",
                AceWindowsCarLocation.PITENTRY to "PitEntry（ピット進入）",
                AceWindowsCarLocation.PITEXIT to "PitExit（ピット退出）",
                AceWindowsCarLocation.TRACK to "Track（コース上）",
                AceWindowsCarLocation.UNKNOWN to "不明",
            )

        // statusとcarLocationの数が異なるため、少ない方のstatusを循環させて両方の全enum値を1ループで網羅する。
        expectedCarLocations.forEachIndexed { index, (carLocation, expectedCarLocationText) ->
            val (status, expectedStatusText) = expectedStatuses[index % expectedStatuses.size]
            rule.setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState =
                            DebugStateDetailUiState(
                                selectedSimulator = Simulator.AceWindows,
                                aceWindowsStatus = AceWindowsStatusData(status = status, carLocation = carLocation),
                                cardOrder = listOf(DebugStateCardKey.VEHICLE_LOCATION),
                            ),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            rule.onNodeWithText("車両位置").assertIsDisplayed()
            rule.onNodeWithText("ステータス: $expectedStatusText").assertIsDisplayed()
            rule.onNodeWithText(expectedCarLocationText).assertIsDisplayed()
        }
    }

    @Test
    fun `selectedSimulatorがLMUの場合はピットレーン走行中とピット状態とガレージ内を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            lmuWindowsPitStatus =
                                LmuWindowsPitStatusData(
                                    inPits = true,
                                    pitState = LmuWindowsPitState.ENTERING,
                                    inGarageStall = false,
                                ),
                            cardOrder = listOf(DebugStateCardKey.VEHICLE_LOCATION),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("車両位置").assertIsDisplayed()
        rule.onNodeWithText("ピットレーン走行中: はい").assertIsDisplayed()
        rule.onNodeWithText("ピット状態: Entering（ピット進入中）").assertIsDisplayed()
        rule.onNodeWithText("ガレージ内: いいえ").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがLMUの場合はpitStateの各値に対応する表示文言を表示する`() {
        val expectedByPitState =
            mapOf(
                LmuWindowsPitState.NONE to "None（ピット動作なし）",
                LmuWindowsPitState.REQUESTED to "Requested（ピット要求中）",
                LmuWindowsPitState.ENTERING to "Entering（ピット進入中）",
                LmuWindowsPitState.STOPPED to "Stopped（ピット停止中）",
                LmuWindowsPitState.EXITING to "Exiting（ピット退出中）",
                LmuWindowsPitState.UNKNOWN to "不明",
            )

        expectedByPitState.forEach { (pitState, expectedText) ->
            rule.setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState =
                            DebugStateDetailUiState(
                                selectedSimulator = Simulator.LmuWindows,
                                lmuWindowsPitStatus =
                                    LmuWindowsPitStatusData(inPits = false, pitState = pitState, inGarageStall = true),
                                cardOrder = listOf(DebugStateCardKey.VEHICLE_LOCATION),
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
