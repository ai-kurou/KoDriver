package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.acewindows.model.AceWindowsBrakeTemperatureData
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.WheelIndex
import org.junit.Rule
import org.junit.Test

class DebugStateBrakeTemperatureCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `ブレーキ温度が未取得の場合は未取得と表示する`() {
        rule.setContent {
            MaterialTheme {
                BrakeTemperatureContent(brakeTemperature = null)
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
                            cardOrder = listOf(AceWindowsDebugStateCardKey.BRAKE_TEMPERATURE),
                            aceWindows =
                                AceWindowsDebugState(
                                    brakeTemperature =
                                        AceWindowsBrakeTemperatureData(
                                            wheels = mapOf(WheelIndex.FRONT_LEFT to CelsiusReading(495.0f)),
                                        ),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("ブレーキ温度").assertIsDisplayed()
        rule.onNodeWithText("FR -℃").assertIsDisplayed()
    }

    @Test
    fun `4輪のブレーキ温度を摂氏で表示する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState =
                        AceWindowsDebugStateDetailUiState(
                            aceWindows =
                                AceWindowsDebugState(
                                    brakeTemperature =
                                        AceWindowsBrakeTemperatureData(
                                            wheels =
                                                mapOf(
                                                    WheelIndex.FRONT_LEFT to CelsiusReading(495.0f),
                                                    WheelIndex.FRONT_RIGHT to CelsiusReading(496.0f),
                                                    WheelIndex.REAR_LEFT to CelsiusReading(497.0f),
                                                    WheelIndex.REAR_RIGHT to CelsiusReading(498.0f),
                                                ),
                                        ),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("ブレーキ温度").assertIsDisplayed()
        rule.onNodeWithText("FL 495.0℃").assertIsDisplayed()
        rule.onNodeWithText("FR 496.0℃").assertIsDisplayed()
        rule.onNodeWithText("RL 497.0℃").assertIsDisplayed()
        rule.onNodeWithText("RR 498.0℃").assertIsDisplayed()
    }
}
