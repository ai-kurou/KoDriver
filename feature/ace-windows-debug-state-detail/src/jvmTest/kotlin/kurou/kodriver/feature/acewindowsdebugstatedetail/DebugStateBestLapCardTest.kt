package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.AceWindowsBestLapTimeData
import org.junit.Rule
import org.junit.Test

class DebugStateBestLapCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `ACEの場合はACEのbestLapTimeMsを表示する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState =
                        AceWindowsDebugStateDetailUiState(
                            cardOrder = listOf(AceWindowsDebugStateCardKey.BEST_LAP),
                            aceWindows =
                                AceWindowsDebugState(
                                    bestLapTime = sampleAceWindowsBestLapTime(bestLapTimeMs = 95_123),
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("自己ベストラップ").assertIsDisplayed()
        rule.onNodeWithText("1:35.123").assertIsDisplayed()
    }

    @Test
    fun `ACEでベストラップが未計測（0以下）の場合は未取得の文言を表示する`() {
        listOf(0, -1).forEach { bestLapTimeMs ->
            rule.setContent {
                MaterialTheme {
                    AceWindowsDebugStateDetailPaneContent(
                        uiState =
                            AceWindowsDebugStateDetailUiState(
                                cardOrder = listOf(AceWindowsDebugStateCardKey.BEST_LAP),
                                aceWindows =
                                    AceWindowsDebugState(
                                        bestLapTime = sampleAceWindowsBestLapTime(bestLapTimeMs = bestLapTimeMs),
                                    ),
                            ),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            rule.onNodeWithText("自己ベストラップ").assertIsDisplayed()
            rule.onNodeWithText("未取得").assertIsDisplayed()
        }
    }

    @Test
    fun `ACEでaceWindowsBestLapTimeがnullの場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState =
                        AceWindowsDebugStateDetailUiState(
                            cardOrder = listOf(AceWindowsDebugStateCardKey.BEST_LAP),
                            aceWindows =
                                AceWindowsDebugState(
                                    bestLapTime = null,
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("自己ベストラップ").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    private fun sampleAceWindowsBestLapTime(bestLapTimeMs: Int) =
        AceWindowsBestLapTimeData(bestLapTimeMs = bestLapTimeMs)
}
