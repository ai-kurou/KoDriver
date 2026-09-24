package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.AceWindowsBrakeWearData
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import org.junit.Rule
import org.junit.Test

class DebugStateBrakeWearCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `selectedSimulatorがLMUの場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            cardOrder = listOf(DebugStateCardKey.BRAKE_WEAR),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("ブレーキ摩耗 (ACEのみ)").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがACEでもデータ未取得の場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.AceWindows,
                            cardOrder = listOf(DebugStateCardKey.BRAKE_WEAR),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("ブレーキ摩耗 (ACEのみ)").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `一部のホイールデータが欠けている場合はハイフンを表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.AceWindows,
                            aceWindowsBrakeWear =
                                AceWindowsBrakeWearData(
                                    padLife = mapOf(WheelIndex.FRONT_LEFT to 0.9),
                                    discLife = mapOf(WheelIndex.FRONT_LEFT to 0.9),
                                ),
                            cardOrder = listOf(DebugStateCardKey.BRAKE_WEAR),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("パッド FR -").assertIsDisplayed()
        rule.onNodeWithText("ディスク FR -").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがACEの場合は4輪分のパッド・ディスク摩耗を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.AceWindows,
                            aceWindowsBrakeWear =
                                AceWindowsBrakeWearData(
                                    padLife =
                                        mapOf(
                                            WheelIndex.FRONT_LEFT to 0.971,
                                            WheelIndex.FRONT_RIGHT to 0.972,
                                            WheelIndex.REAR_LEFT to 0.973,
                                            WheelIndex.REAR_RIGHT to 0.974,
                                        ),
                                    discLife =
                                        mapOf(
                                            WheelIndex.FRONT_LEFT to 0.881,
                                            WheelIndex.FRONT_RIGHT to 0.882,
                                            WheelIndex.REAR_LEFT to 0.883,
                                            WheelIndex.REAR_RIGHT to 0.884,
                                        ),
                                ),
                            cardOrder = listOf(DebugStateCardKey.BRAKE_WEAR),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("ブレーキ摩耗 (ACEのみ)").assertIsDisplayed()
        rule.onNodeWithText("パッド FL 0.971").assertIsDisplayed()
        rule.onNodeWithText("パッド FR 0.972").assertIsDisplayed()
        rule.onNodeWithText("パッド RL 0.973").assertIsDisplayed()
        rule.onNodeWithText("パッド RR 0.974").assertIsDisplayed()
        rule.onNodeWithText("ディスク FL 0.881").assertIsDisplayed()
        rule.onNodeWithText("ディスク FR 0.882").assertIsDisplayed()
        rule.onNodeWithText("ディスク RL 0.883").assertIsDisplayed()
        rule.onNodeWithText("ディスク RR 0.884").assertIsDisplayed()
    }
}
