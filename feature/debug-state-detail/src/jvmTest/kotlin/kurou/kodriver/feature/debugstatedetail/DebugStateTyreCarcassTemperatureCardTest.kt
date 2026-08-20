package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import org.junit.Rule
import org.junit.Test

class DebugStateTyreCarcassTemperatureCardTest {
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
                            tyreCarcassTemperature =
                                LmuWindowsTyreCarcassTemperatureData(
                                    wheels = mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(95.0f)),
                                ),
                            cardOrder = listOf(DebugStateCardKey.TYRE_CARCASS_TEMPERATURE),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("タイヤカーカス温度").assertIsDisplayed()
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
                            tyreCarcassTemperature =
                                LmuWindowsTyreCarcassTemperatureData(
                                    wheels = mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(95.0f)),
                                ),
                            cardOrder = listOf(DebugStateCardKey.TYRE_CARCASS_TEMPERATURE),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("タイヤカーカス温度").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがACEの場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.AceWindows,
                            tyreCarcassTemperature =
                                LmuWindowsTyreCarcassTemperatureData(
                                    wheels = mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(95.0f)),
                                ),
                            cardOrder = listOf(DebugStateCardKey.TYRE_CARCASS_TEMPERATURE),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("タイヤカーカス温度").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `一部のホイールデータが欠けている場合はハイフンを表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            tyreCarcassTemperature =
                                LmuWindowsTyreCarcassTemperatureData(
                                    wheels = mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(95.0f)),
                                ),
                            cardOrder = listOf(DebugStateCardKey.TYRE_CARCASS_TEMPERATURE),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("タイヤカーカス温度").assertIsDisplayed()
        rule.onNodeWithText("FR -℃").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがLMUの場合は4輪のカーカス温度を摂氏で表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            tyreCarcassTemperature =
                                LmuWindowsTyreCarcassTemperatureData(
                                    wheels =
                                        mapOf(
                                            WheelIndex.FRONT_LEFT to CelsiusReading(95.0f),
                                            WheelIndex.FRONT_RIGHT to CelsiusReading(96.0f),
                                            WheelIndex.REAR_LEFT to CelsiusReading(97.0f),
                                            WheelIndex.REAR_RIGHT to CelsiusReading(98.0f),
                                        ),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("タイヤカーカス温度").assertIsDisplayed()
        rule.onNodeWithText("FL 95.0℃").assertIsDisplayed()
        rule.onNodeWithText("FR 96.0℃").assertIsDisplayed()
        rule.onNodeWithText("RL 97.0℃").assertIsDisplayed()
        rule.onNodeWithText("RR 98.0℃").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがACEの場合は4輪のカーカス温度を摂氏で表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.AceWindows,
                            aceWindowsTyreCarcassTemperature =
                                AceWindowsTyreCarcassTemperatureData(
                                    wheels =
                                        mapOf(
                                            WheelIndex.FRONT_LEFT to CelsiusReading(91.0f),
                                            WheelIndex.FRONT_RIGHT to CelsiusReading(92.0f),
                                            WheelIndex.REAR_LEFT to CelsiusReading(93.0f),
                                            WheelIndex.REAR_RIGHT to CelsiusReading(94.0f),
                                        ),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("タイヤカーカス温度").assertIsDisplayed()
        rule.onNodeWithText("FL 91.0℃").assertIsDisplayed()
        rule.onNodeWithText("FR 92.0℃").assertIsDisplayed()
        rule.onNodeWithText("RL 93.0℃").assertIsDisplayed()
        rule.onNodeWithText("RR 94.0℃").assertIsDisplayed()
    }
}
