package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData
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
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.TYRE_CARCASS_TEMPERATURE),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    tyreCarcassTemperature =
                                        LmuWindowsTyreCarcassTemperatureData(
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
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            lmuWindows =
                                LmuWindowsDebugState(
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
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        val cardIndex = defaultDebugStateCardOrder.indexOf(LmuWindowsDebugStateCardKey.TYRE_CARCASS_TEMPERATURE)
        rule.onNodeWithTag(DEBUG_STATE_GRID_TEST_TAG).performScrollToIndex(cardIndex)
        rule.onNodeWithText("タイヤカーカス温度").assertIsDisplayed()
        rule.onNodeWithText("FL 95.0℃").assertIsDisplayed()
        rule.onNodeWithText("FR 96.0℃").assertIsDisplayed()
        rule.onNodeWithText("RL 97.0℃").assertIsDisplayed()
        rule.onNodeWithText("RR 98.0℃").assertIsDisplayed()
    }
}
