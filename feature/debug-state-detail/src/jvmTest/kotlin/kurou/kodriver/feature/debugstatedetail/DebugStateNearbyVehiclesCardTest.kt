package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.LmuWindowsNearbyVehicleData
import kurou.kodriver.domain.model.LmuWindowsNearbyVehiclesData
import org.junit.Rule
import org.junit.Test

class DebugStateNearbyVehiclesCardTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `10m以内の車両カードのタイトルを表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("10m以内の車両").assertIsDisplayed()
    }

    @Test
    fun `nearbyVehiclesが未取得の場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(
                        nearbyVehicles = null,
                        cardOrder = listOf(DebugStateCardKey.NEARBY_VEHICLES),
                    ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `10m以内に車両がいない場合は車両なしの文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(
                        nearbyVehicles = LmuWindowsNearbyVehiclesData(vehicles = emptyList()),
                    ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("10m以内に車両なし").assertIsDisplayed()
    }

    @Test
    fun `前方右にいる車両は前方右の距離を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(
                        nearbyVehicles = LmuWindowsNearbyVehiclesData(
                            vehicles = listOf(
                                LmuWindowsNearbyVehicleData(
                                    vehicleId = 4,
                                    longitudinalDistanceMeters = 3.0,
                                    lateralDistanceMeters = 2.0,
                                ),
                            ),
                        ),
                    ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("#4 前方3.0m 右2.0m").assertIsDisplayed()
    }

    @Test
    fun `後方左にいる車両は後方左の距離を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(
                        nearbyVehicles = LmuWindowsNearbyVehiclesData(
                            vehicles = listOf(
                                LmuWindowsNearbyVehicleData(
                                    vehicleId = 7,
                                    longitudinalDistanceMeters = -1.5,
                                    lateralDistanceMeters = -4.2,
                                ),
                            ),
                        ),
                    ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("#7 後方1.5m 左4.2m").assertIsDisplayed()
    }

    @Test
    fun `複数台の車両情報を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState = DebugStateDetailUiState(
                        nearbyVehicles = LmuWindowsNearbyVehiclesData(
                            vehicles = listOf(
                                LmuWindowsNearbyVehicleData(
                                    vehicleId = 1,
                                    longitudinalDistanceMeters = 1.0,
                                    lateralDistanceMeters = 1.0,
                                ),
                                LmuWindowsNearbyVehicleData(
                                    vehicleId = 2,
                                    longitudinalDistanceMeters = -2.0,
                                    lateralDistanceMeters = -2.0,
                                ),
                            ),
                        ),
                    ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("#1 前方1.0m 右1.0m").assertIsDisplayed()
        rule.onNodeWithText("#2 後方2.0m 左2.0m").assertIsDisplayed()
    }
}
