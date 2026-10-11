package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.AceWindowsRemainingFuelLapsData
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.FuelPercent
import org.junit.Rule
import org.junit.Test

class DebugStateFuelConsumptionCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `ACEでデータ未取得の場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState =
                        AceWindowsDebugStateDetailUiState(
                            cardOrder = listOf(DebugStateCardKey.FUEL_CONSUMPTION),
                            aceWindows =
                                AceWindowsDebugState(
                                    fuel = null,
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("燃料消費").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `ACEの場合は残り燃料の割合を表示する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState =
                        AceWindowsDebugStateDetailUiState(
                            cardOrder = listOf(DebugStateCardKey.FUEL_CONSUMPTION),
                            aceWindows =
                                AceWindowsDebugState(
                                    fuel = AceWindowsFuelData(remainingPercent = FuelPercent(42.0)),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("燃料消費").assertIsDisplayed()
        rule.onNodeWithText("残量 42.0%").assertIsDisplayed()
    }

    @Test
    fun `ACEの場合は残り燃料の割合と残り周数を表示する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState =
                        AceWindowsDebugStateDetailUiState(
                            cardOrder = listOf(DebugStateCardKey.FUEL_CONSUMPTION),
                            aceWindows =
                                AceWindowsDebugState(
                                    fuel = AceWindowsFuelData(remainingPercent = FuelPercent(42.0)),
                                    remainingFuelLaps = AceWindowsRemainingFuelLapsData(remainingLaps = 3.46f),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("残量 42.0%").assertIsDisplayed()
        rule.onNodeWithText("残り 3.5周").assertIsDisplayed()
    }

    @Test
    fun `ACEで残り周数が0の場合は残り周数を表示しない`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState =
                        AceWindowsDebugStateDetailUiState(
                            cardOrder = listOf(DebugStateCardKey.FUEL_CONSUMPTION),
                            aceWindows =
                                AceWindowsDebugState(
                                    fuel = AceWindowsFuelData(remainingPercent = FuelPercent(42.0)),
                                    remainingFuelLaps = AceWindowsRemainingFuelLapsData(remainingLaps = 0f),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("残量 42.0%").assertIsDisplayed()
        rule.onNodeWithText("残り", substring = true).assertDoesNotExist()
    }

    @Test
    fun `ACEで残り周数が非有限値の場合は残り周数を表示しない`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState =
                        AceWindowsDebugStateDetailUiState(
                            cardOrder = listOf(DebugStateCardKey.FUEL_CONSUMPTION),
                            aceWindows =
                                AceWindowsDebugState(
                                    fuel = AceWindowsFuelData(remainingPercent = FuelPercent(42.0)),
                                    remainingFuelLaps = AceWindowsRemainingFuelLapsData(remainingLaps = Float.NaN),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("残量 42.0%").assertIsDisplayed()
        rule.onNodeWithText("残り", substring = true).assertDoesNotExist()
    }
}
