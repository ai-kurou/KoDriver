package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.BrakeThicknessMeters
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.model.LmuWindowsBrakeWearWheelRemaining
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.WheelIndex
import org.junit.Rule
import org.junit.Test

class DebugStateBrakeWearCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `LMUはブレーキ残量と欠損ホイールを既存形式で表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            brakeWear =
                                LmuWindowsBrakeWearRemainingData(
                                    wheels =
                                        mapOf(
                                            WheelIndex.FRONT_LEFT to
                                                LmuWindowsBrakeWearWheelRemaining(BrakeThicknessMeters(0.036f), 87f),
                                            WheelIndex.FRONT_RIGHT to
                                                LmuWindowsBrakeWearWheelRemaining(
                                                    BrakeThicknessMeters(0.0305f),
                                                    50.06f,
                                                ),
                                            WheelIndex.REAR_LEFT to
                                                LmuWindowsBrakeWearWheelRemaining(BrakeThicknessMeters(-0.001f), -1f),
                                        ),
                                ),
                            cardOrder = listOf(DebugStateCardKey.BRAKE_WEAR),
                        ),
                    canNavigateBack = false,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("FL 87.0%(36.000mm)").assertIsDisplayed()
        rule.onNodeWithText("FR 50.1%(30.500mm)").assertIsDisplayed()
        rule.onNodeWithText("RL 0.0%(0.000mm)").assertIsDisplayed()
        rule.onNodeWithText("RR --").assertIsDisplayed()
    }

    @Test
    fun `対応外シミュレーターと未取得のLMUは取得できませんと表示する`() {
        listOf(Simulator.Gt7Ps5, Simulator.AceWindows, Simulator.LmuWindows).forEach { simulator ->
            rule.setContent {
                MaterialTheme {
                    BrakeWearContent(
                        selectedSimulator = simulator,
                        brakeWear =
                            if (simulator is Simulator.LmuWindows) {
                                null
                            } else {
                                LmuWindowsBrakeWearRemainingData(
                                    mapOf(
                                        WheelIndex.FRONT_LEFT to
                                            LmuWindowsBrakeWearWheelRemaining(BrakeThicknessMeters(0.036f), 87f),
                                    ),
                                )
                            },
                    )
                }
            }

            rule.onNodeWithText("取得できません").assertIsDisplayed()
        }
    }
}
