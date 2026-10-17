package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.acewindows.model.AceWindowsNearbyVehicleData
import kurou.kodriver.domain.acewindows.model.AceWindowsVehicleApproachData
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

class DebugStateSideBySideVehiclesCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `ACEでaceWindowsVehicleApproachがnullの場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState =
                        AceWindowsDebugStateDetailUiState(
                            cardOrder = listOf(AceWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES),
                            aceWindows =
                                AceWindowsDebugState(
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
    fun `ACEで周辺車両がいない場合はなしの文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState =
                        AceWindowsDebugStateDetailUiState(
                            cardOrder = listOf(AceWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES),
                            aceWindows =
                                AceWindowsDebugState(
                                    vehicleApproach = AceWindowsVehicleApproachData(nearbyVehicles = emptyList()),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("周辺車両なし").assertIsDisplayed()
    }

    @Test
    fun `ACEで複数の周辺車両がいる場合は距離の近い順に表示する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState =
                        AceWindowsDebugStateDetailUiState(
                            cardOrder = listOf(AceWindowsDebugStateCardKey.SIDE_BY_SIDE_VEHICLES),
                            aceWindows =
                                AceWindowsDebugState(
                                    vehicleApproach =
                                        AceWindowsVehicleApproachData(
                                            nearbyVehicles =
                                                listOf(
                                                    AceWindowsNearbyVehicleData(distanceMeters = 12.34),
                                                    AceWindowsNearbyVehicleData(distanceMeters = 3.06),
                                                ),
                                        ),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("3.1m").assertIsDisplayed()
        rule.onNodeWithText("12.3m").assertIsDisplayed()

        val nearerTop =
            rule
                .onNodeWithText("3.1m")
                .fetchSemanticsNode()
                .boundsInRoot.top
        val fartherTop =
            rule
                .onNodeWithText("12.3m")
                .fetchSemanticsNode()
                .boundsInRoot.top
        assertTrue(nearerTop < fartherTop)
    }
}
