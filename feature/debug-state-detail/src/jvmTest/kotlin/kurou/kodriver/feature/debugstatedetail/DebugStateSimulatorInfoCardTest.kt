package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test

class DebugStateSimulatorInfoCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `selectedSimulatorがLMUの場合はLMUの表示名を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            cardOrder = listOf(DebugStateCardKey.SIMULATOR),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("Le Mans Ultimate（Windows版）").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがGT7の場合はGT7の表示名を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.Gt7Ps5,
                            cardOrder = listOf(DebugStateCardKey.SIMULATOR),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("Gran Turismo 7（PS5）").assertIsDisplayed()
    }

    @Test
    fun `selectedSimulatorがACEの場合はACEの表示名を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            selectedSimulator = Simulator.AceWindows,
                            cardOrder = listOf(DebugStateCardKey.SIMULATOR),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("Assetto Corsa EVO（Windows版）").assertIsDisplayed()
    }
}
