package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.BrakeThicknessMeters
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.model.LmuWindowsBrakeWearWheelRemaining
import kurou.kodriver.domain.model.WheelIndex
import org.junit.Rule
import org.junit.Test

class DebugStateBrakeWearCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `未取得の場合は取得できませんと表示する`() {
        rule.setContent {
            MaterialTheme {
                BrakeWearContent(brakeWear = null)
            }
        }

        rule.onNodeWithText("取得できません").assertIsDisplayed()
    }

    @Test
    fun `LMUはブレーキ残量と欠損ホイールを既存形式で表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(DebugStateCardKey.BRAKE_WEAR),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    brakeWear =
                                        LmuWindowsBrakeWearRemainingData(
                                            wheels =
                                                mapOf(
                                                    WheelIndex.FRONT_LEFT to
                                                        LmuWindowsBrakeWearWheelRemaining(
                                                            BrakeThicknessMeters(0.036f),
                                                            87f,
                                                        ),
                                                    WheelIndex.FRONT_RIGHT to
                                                        LmuWindowsBrakeWearWheelRemaining(
                                                            BrakeThicknessMeters(0.0305f),
                                                            50.06f,
                                                        ),
                                                    WheelIndex.REAR_LEFT to
                                                        LmuWindowsBrakeWearWheelRemaining(
                                                            BrakeThicknessMeters(-0.001f),
                                                            -1f,
                                                        ),
                                                ),
                                        ),
                                ),
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
}
