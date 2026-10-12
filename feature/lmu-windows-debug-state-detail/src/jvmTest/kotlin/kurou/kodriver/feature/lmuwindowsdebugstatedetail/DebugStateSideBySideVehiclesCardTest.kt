package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.LateralDistanceMeters
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

class DebugStateSideBySideVehiclesCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `vehicleApproachがnullの場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    vehicleApproach = null,
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("並走車両").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `LMUで左右どちらにも並走車両がいない場合はなしの文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    vehicleApproach =
                                        LmuWindowsVehicleApproachData(
                                            sideBySideLeftVehicleIds = emptySet(),
                                            sideBySideRightVehicleIds = emptySet(),
                                            lateralDistanceLeftMeters = LateralDistanceMeters(Double.MAX_VALUE),
                                            lateralDistanceRightMeters = LateralDistanceMeters(Double.MAX_VALUE),
                                        ),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("並走車両なし").assertIsDisplayed()
    }

    @Test
    fun `LMUで左側のみ並走車両がいる場合は左側の距離のみ表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    vehicleApproach =
                                        LmuWindowsVehicleApproachData(
                                            sideBySideLeftVehicleIds = setOf(1),
                                            sideBySideRightVehicleIds = emptySet(),
                                            lateralDistanceLeftMeters = LateralDistanceMeters(1.24),
                                            lateralDistanceRightMeters = LateralDistanceMeters(Double.MAX_VALUE),
                                        ),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("左1.2m").assertIsDisplayed()
    }

    @Test
    fun `LMUで右側のみ並走車両がいる場合は右側の距離のみ表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    sideBySideDurations = LmuWindowsSideBySideDurations(1_000, 0),
                                    vehicleApproach =
                                        LmuWindowsVehicleApproachData(
                                            sideBySideLeftVehicleIds = emptySet(),
                                            sideBySideRightVehicleIds = setOf(2),
                                            lateralDistanceLeftMeters = LateralDistanceMeters(Double.MAX_VALUE),
                                            lateralDistanceRightMeters = LateralDistanceMeters(2.06),
                                        ),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("右2.1m").assertIsDisplayed()
        rule.onNodeWithText("0.0秒").assertIsDisplayed()
        rule.onNodeWithText("1.0秒").assertDoesNotExist()
    }

    @Test
    fun `LMUで左右両方に並走車両がいる場合は両側の距離を表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    vehicleApproach =
                                        LmuWindowsVehicleApproachData(
                                            sideBySideLeftVehicleIds = setOf(1),
                                            sideBySideRightVehicleIds = setOf(2),
                                            lateralDistanceLeftMeters = LateralDistanceMeters(0.5),
                                            lateralDistanceRightMeters = LateralDistanceMeters(0.76),
                                        ),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("左0.5m").assertIsDisplayed()
        rule.onNodeWithText("右0.8m").assertIsDisplayed()
    }

    @Test
    fun `LMUで左右両方に並走車両がいる場合は両側の距離の下に秒数を表示する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES),
                            lmuWindows =
                                LmuWindowsDebugState(
                                    sideBySideDurations = LmuWindowsSideBySideDurations(1_240, 2_060),
                                    vehicleApproach =
                                        LmuWindowsVehicleApproachData(
                                            sideBySideLeftVehicleIds = setOf(1),
                                            sideBySideRightVehicleIds = setOf(2),
                                            lateralDistanceLeftMeters = LateralDistanceMeters(0.5),
                                            lateralDistanceRightMeters = LateralDistanceMeters(0.76),
                                        ),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("左0.5m").assertIsDisplayed()
        rule.onNodeWithText("右0.8m").assertIsDisplayed()
        rule.onNodeWithText("1.2秒").assertIsDisplayed()
        rule.onNodeWithText("2.1秒").assertIsDisplayed()
        val leftDistanceTop = topOf("左0.5m")
        val leftDurationTop = topOf("1.2秒")
        val rightDistanceTop = topOf("右0.8m")
        val rightDurationTop = topOf("2.1秒")
        assertTrue(leftDurationTop > leftDistanceTop)
        assertTrue(rightDurationTop > rightDistanceTop)
    }

    private fun topOf(text: String): Float {
        val node = rule.onNodeWithText(text).fetchSemanticsNode()
        return node.boundsInRoot.top
    }
}
