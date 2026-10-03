package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import org.junit.Rule
import org.junit.Test

class DebugStateBrakeTemperatureCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `LMUはブレーキ温度と欠損ホイールを既存形式で表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            brakeTemperature =
                                LmuWindowsBrakeTemperatureData(
                                    wheels =
                                        mapOf(
                                            WheelIndex.FRONT_LEFT to CelsiusReading(399f),
                                            WheelIndex.FRONT_RIGHT to CelsiusReading(600f),
                                            WheelIndex.REAR_LEFT to CelsiusReading(800f),
                                        ),
                                ),
                            cardOrder = listOf(DebugStateCardKey.BRAKE_TEMPERATURE),
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

    @Test
    fun `対応外シミュレーターと未取得のLMUは未取得表示になる`() {
        listOf(Simulator.Gt7Ps5, Simulator.AceWindows, Simulator.LmuWindows).forEach { simulator ->
            rule.setContent {
                MaterialTheme {
                    BrakeTemperatureContent(selectedSimulator = simulator, brakeTemperature = null)
                }
            }

            rule.onNodeWithText("未取得").assertIsDisplayed()
        }
    }
}
