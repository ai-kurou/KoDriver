package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.LateralDistanceMeters
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import org.junit.Rule
import org.junit.Test

class DebugStateSideBySideVehiclesCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `vehicleApproachがnullの場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            vehicleApproach = null,
                            cardOrder = listOf(DebugStateCardKey.SIDE_BY_SIDE_VEHICLES),
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
    fun `左右どちらにも並走車両がいない場合はなしの文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            vehicleApproach =
                                LmuWindowsVehicleApproachData(
                                    sideBySideLeftVehicleIds = emptySet(),
                                    sideBySideRightVehicleIds = emptySet(),
                                    lateralDistanceLeftMeters = LateralDistanceMeters(Double.MAX_VALUE),
                                    lateralDistanceRightMeters = LateralDistanceMeters(Double.MAX_VALUE),
                                ),
                            cardOrder = listOf(DebugStateCardKey.SIDE_BY_SIDE_VEHICLES),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("並走車両なし").assertIsDisplayed()
    }

    @Test
    fun `左側のみ並走車両がいる場合は左側の距離のみ表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            vehicleApproach =
                                LmuWindowsVehicleApproachData(
                                    sideBySideLeftVehicleIds = setOf(1),
                                    sideBySideRightVehicleIds = emptySet(),
                                    lateralDistanceLeftMeters = LateralDistanceMeters(1.24),
                                    lateralDistanceRightMeters = LateralDistanceMeters(Double.MAX_VALUE),
                                ),
                            cardOrder = listOf(DebugStateCardKey.SIDE_BY_SIDE_VEHICLES),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("左1.2m").assertIsDisplayed()
    }

    @Test
    fun `右側のみ並走車両がいる場合は右側の距離のみ表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            vehicleApproach =
                                LmuWindowsVehicleApproachData(
                                    sideBySideLeftVehicleIds = emptySet(),
                                    sideBySideRightVehicleIds = setOf(2),
                                    lateralDistanceLeftMeters = LateralDistanceMeters(Double.MAX_VALUE),
                                    lateralDistanceRightMeters = LateralDistanceMeters(2.06),
                                ),
                            cardOrder = listOf(DebugStateCardKey.SIDE_BY_SIDE_VEHICLES),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("右2.1m").assertIsDisplayed()
    }

    @Test
    fun `左右両方に並走車両がいる場合は両側の距離を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            vehicleApproach =
                                LmuWindowsVehicleApproachData(
                                    sideBySideLeftVehicleIds = setOf(1),
                                    sideBySideRightVehicleIds = setOf(2),
                                    lateralDistanceLeftMeters = LateralDistanceMeters(0.5),
                                    lateralDistanceRightMeters = LateralDistanceMeters(0.76),
                                ),
                            cardOrder = listOf(DebugStateCardKey.SIDE_BY_SIDE_VEHICLES),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("左0.5m").assertIsDisplayed()
        rule.onNodeWithText("右0.8m").assertIsDisplayed()
    }
}
