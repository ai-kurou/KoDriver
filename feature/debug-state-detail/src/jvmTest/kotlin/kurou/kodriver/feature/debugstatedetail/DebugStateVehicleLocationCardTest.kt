package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.domain.model.AceWindowsCarLocation
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.AceWindowsStatusType
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.LmuWindowsPitState
import kurou.kodriver.domain.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.model.Simulator
import org.junit.Test

class DebugStateVehicleLocationCardTest {
    @Test
    fun `selectedSimulatorが未選択の場合は未取得の文言を表示する`() =
        composeScreenshotTest {
            setContent {
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

            onNodeWithText("車両位置").assertIsDisplayed()
            onNodeWithText("未取得").assertIsDisplayed()
        }

    @Test
    fun `selectedSimulatorがACEでステータス情報が未取得の場合は未取得の文言を表示する`() =
        composeScreenshotTest {
            setContent {
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

            onNodeWithText("車両位置").assertIsDisplayed()
            onNodeWithText("未取得").assertIsDisplayed()
        }

    @Test
    fun `selectedSimulatorがLMUでピット状態が未取得の場合は未取得の文言を表示する`() =
        composeScreenshotTest {
            setContent {
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

            onNodeWithText("車両位置").assertIsDisplayed()
            onNodeWithText("未取得").assertIsDisplayed()
        }

    @Test
    fun `selectedSimulatorがGT7の場合は未取得の文言を表示する`() =
        composeScreenshotTest {
            setContent {
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

            onNodeWithText("車両位置").assertIsDisplayed()
            onNodeWithText("未取得").assertIsDisplayed()
        }

    @Test
    fun `selectedSimulatorがACEの場合はstatusとcarLocationの各値に対応する表示文言を表示する`() =
        composeScreenshotTest {
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
                setContent {
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

                onNodeWithText("車両位置").assertIsDisplayed()
                onNodeWithText("ステータス: $expectedStatusText").assertIsDisplayed()
                onNodeWithText(expectedCarLocationText).assertIsDisplayed()
            }
        }

    @Test
    fun `selectedSimulatorがLMUの場合はピットレーン走行中とピット状態とガレージ内を表示する`() =
        composeScreenshotTest {
            setContent {
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

            onNodeWithText("車両位置").assertIsDisplayed()
            onNodeWithText("ピットレーン走行中: はい").assertIsDisplayed()
            onNodeWithText("ピット状態: Entering（ピット進入中）").assertIsDisplayed()
            onNodeWithText("ガレージ内: いいえ").assertIsDisplayed()
        }

    @Test
    fun `selectedSimulatorがLMUの場合はpitStateの各値に対応する表示文言を表示する`() =
        composeScreenshotTest {
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
                setContent {
                    MaterialTheme {
                        DebugStateDetailPaneContent(
                            uiState =
                                DebugStateDetailUiState(
                                    selectedSimulator = Simulator.LmuWindows,
                                    lmuWindowsPitStatus =
                                        LmuWindowsPitStatusData(
                                            inPits = false,
                                            pitState = pitState,
                                            inGarageStall = true,
                                        ),
                                    cardOrder = listOf(DebugStateCardKey.VEHICLE_LOCATION),
                                ),
                            canNavigateBack = true,
                            onBack = {},
                        )
                    }
                }

                onNodeWithText("車両位置").assertIsDisplayed()
                onNodeWithText("ピット状態: $expectedText").assertIsDisplayed()
                onNodeWithText("ピットレーン走行中: いいえ").assertIsDisplayed()
                onNodeWithText("ガレージ内: はい").assertIsDisplayed()
            }
        }
}
