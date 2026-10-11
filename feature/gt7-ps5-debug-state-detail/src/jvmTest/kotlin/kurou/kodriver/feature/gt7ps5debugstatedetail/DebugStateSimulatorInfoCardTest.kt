package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.DebugStateCardKey
import org.junit.Rule
import org.junit.Test

class DebugStateSimulatorInfoCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `GT7の表示名を表示する`() {
        rule.setContent {
            MaterialTheme {
                Gt7Ps5DebugStateDetailPaneContent(
                    uiState =
                        Gt7Ps5DebugStateDetailUiState(
                            cardOrder = listOf(DebugStateCardKey.SIMULATOR),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("Gran Turismo 7（PS5）").assertIsDisplayed()
    }
}
