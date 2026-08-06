package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import org.junit.Test

class DebugStateTyreCarcassTemperatureCardTest {
    @Test
    fun `selectedSimulatorが未選択の場合は未取得の文言を表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState =
                            DebugStateDetailUiState(
                                selectedSimulator = null,
                                tyreCarcassTemperature =
                                    LmuWindowsTyreCarcassTemperatureData(
                                        wheels = mapOf(WheelIndex.FRONT_LEFT to 95.0),
                                    ),
                                cardOrder = listOf(DebugStateCardKey.TYRE_CARCASS_TEMPERATURE),
                            ),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            onNodeWithText("タイヤカーカス温度").assertIsDisplayed()
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
                                tyreCarcassTemperature =
                                    LmuWindowsTyreCarcassTemperatureData(
                                        wheels = mapOf(WheelIndex.FRONT_LEFT to 95.0),
                                    ),
                                cardOrder = listOf(DebugStateCardKey.TYRE_CARCASS_TEMPERATURE),
                            ),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            onNodeWithText("タイヤカーカス温度").assertIsDisplayed()
            onNodeWithText("未取得").assertIsDisplayed()
        }

    @Test
    fun `selectedSimulatorがACEの場合は未取得の文言を表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState =
                            DebugStateDetailUiState(
                                selectedSimulator = Simulator.AceWindows,
                                tyreCarcassTemperature =
                                    LmuWindowsTyreCarcassTemperatureData(
                                        wheels = mapOf(WheelIndex.FRONT_LEFT to 95.0),
                                    ),
                                cardOrder = listOf(DebugStateCardKey.TYRE_CARCASS_TEMPERATURE),
                            ),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            onNodeWithText("タイヤカーカス温度").assertIsDisplayed()
            onNodeWithText("未取得").assertIsDisplayed()
        }

    @Test
    fun `一部のホイールデータが欠けている場合はハイフンを表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState =
                            DebugStateDetailUiState(
                                selectedSimulator = Simulator.LmuWindows,
                                tyreCarcassTemperature =
                                    LmuWindowsTyreCarcassTemperatureData(
                                        wheels = mapOf(WheelIndex.FRONT_LEFT to 95.0),
                                    ),
                                cardOrder = listOf(DebugStateCardKey.TYRE_CARCASS_TEMPERATURE),
                            ),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            onNodeWithText("タイヤカーカス温度").assertIsDisplayed()
            onNodeWithText("FR -℃").assertIsDisplayed()
        }

    @Test
    fun `selectedSimulatorがLMUの場合は4輪のカーカス温度を摂氏で表示する`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState =
                            DebugStateDetailUiState(
                                selectedSimulator = Simulator.LmuWindows,
                                tyreCarcassTemperature =
                                    LmuWindowsTyreCarcassTemperatureData(
                                        wheels =
                                            mapOf(
                                                WheelIndex.FRONT_LEFT to 95.0,
                                                WheelIndex.FRONT_RIGHT to 96.0,
                                                WheelIndex.REAR_LEFT to 97.0,
                                                WheelIndex.REAR_RIGHT to 98.0,
                                            ),
                                    ),
                            ),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            onNodeWithText("タイヤカーカス温度").assertIsDisplayed()
            onNodeWithText("FL 95.0℃").assertIsDisplayed()
            onNodeWithText("FR 96.0℃").assertIsDisplayed()
            onNodeWithText("RL 97.0℃").assertIsDisplayed()
            onNodeWithText("RR 98.0℃").assertIsDisplayed()
        }
}
