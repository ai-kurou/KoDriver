package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.WheelIndex
import org.junit.Rule
import org.junit.Test

class DebugStateBrakeTemperatureCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `未取得の場合は未取得と表示する`() {
        rule.setContent {
            MaterialTheme {
                BrakeTemperatureContent(brakeTemperature = null)
            }
        }

        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `LMUはブレーキ温度と欠損ホイールを既存形式で表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.BRAKE_TEMPERATURE),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    brakeTemperature =
                                        LmuWindowsBrakeTemperatureData(
                                            wheels =
                                                mapOf(
                                                    WheelIndex.FRONT_LEFT to CelsiusReading(399f),
                                                    WheelIndex.FRONT_RIGHT to CelsiusReading(600f),
                                                    WheelIndex.REAR_LEFT to CelsiusReading(800f),
                                                ),
                                        ),
                                ),
                        ),
                    canNavigateBack = false,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("FL 399.0℃").assertIsDisplayed()
        rule.onNodeWithText("FR 600.0℃").assertIsDisplayed()
        rule.onNodeWithText("RL 800.0℃").assertIsDisplayed()
        rule.onNodeWithText("RR -℃").assertIsDisplayed()
    }
}
