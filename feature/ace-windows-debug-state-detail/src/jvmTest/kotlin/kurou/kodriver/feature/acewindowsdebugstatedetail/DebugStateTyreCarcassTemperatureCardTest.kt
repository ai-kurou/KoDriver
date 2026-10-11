package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.WheelIndex
import org.junit.Rule
import org.junit.Test

class DebugStateTyreCarcassTemperatureCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `カーカス温度が未取得の場合は未取得と表示する`() {
        rule.setContent {
            MaterialTheme {
                TyreCarcassTemperatureContent(tyreCarcassTemperature = null)
            }
        }

        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `一部のホイールデータが欠けている場合はハイフンを表示する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState =
                        AceWindowsDebugStateDetailUiState(
                            cardOrder = listOf(DebugStateCardKey.TYRE_CARCASS_TEMPERATURE),
                            aceWindows =
                                AceWindowsDebugState(
                                    tyreCarcassTemperature =
                                        AceWindowsTyreCarcassTemperatureData(
                                            wheels = mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(95.0f)),
                                        ),
                                ),
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
    fun `4輪のカーカス温度を摂氏で表示する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState =
                        AceWindowsDebugStateDetailUiState(
                            aceWindows =
                                AceWindowsDebugState(
                                    tyreCarcassTemperature =
                                        AceWindowsTyreCarcassTemperatureData(
                                            wheels =
                                                mapOf(
                                                    WheelIndex.FRONT_LEFT to CelsiusReading(95.0f),
                                                    WheelIndex.FRONT_RIGHT to CelsiusReading(96.0f),
                                                    WheelIndex.REAR_LEFT to CelsiusReading(97.0f),
                                                    WheelIndex.REAR_RIGHT to CelsiusReading(98.0f),
                                                ),
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
}
