package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyRatio
import org.junit.Rule
import org.junit.Test

class DebugStateSessionCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `virtualEnergyがnullの場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPaneContent(
                    uiState =
                        DebugStateDetailUiState(
                            virtualEnergy = null,
                            cardOrder = listOf(DebugStateCardKey.SESSION),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("セッション (LMUのみ)").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `sessionの値ごとに対応する表示名を表示する`() {
        val expected =
            mapOf(
                0 to "Test Day（テスト走行）",
                1 to "Practice（練習走行）",
                4 to "Practice（練習走行）",
                5 to "Qualifying（予選）",
                8 to "Qualifying（予選）",
                9 to "Warmup（ウォームアップ）",
                10 to "Race（決勝）",
                13 to "Race（決勝）",
                14 to "不明",
                -1 to "不明",
            )

        expected.forEach { (session, displayName) ->
            rule.setContent {
                MaterialTheme {
                    DebugStateDetailPaneContent(
                        uiState =
                            DebugStateDetailUiState(
                                virtualEnergy =
                                    LmuWindowsVirtualEnergyData(
                                        remainingRatio = LmuWindowsVirtualEnergyRatio(0.0),
                                        session = session,
                                    ),
                                cardOrder = listOf(DebugStateCardKey.SESSION),
                            ),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            rule.onNodeWithText(displayName).assertIsDisplayed()
        }
    }
}
