package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.acewindows.model.AceWindowsGapData
import org.junit.Rule
import org.junit.Test

class DebugStateCarGapCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `前後車両との差を符号を保持して表示する`() {
        showCarGapCard(AceWindowsGapData(1.0f, -2.34f))

        rule.onNodeWithText("前後車両との時間差").assertIsDisplayed()
        rule.onNodeWithText("前車との差 1.0 秒").assertIsDisplayed()
        rule.onNodeWithText("後車との差 -2.3 秒").assertIsDisplayed()
    }

    @Test
    fun `非有限値は欠損表示にする`() {
        showCarGapCard(AceWindowsGapData(Float.NaN, Float.POSITIVE_INFINITY))

        rule.onNodeWithText("前車との差 -- 秒").assertIsDisplayed()
        rule.onNodeWithText("後車との差 -- 秒").assertIsDisplayed()
    }

    @Test
    fun `ゼロと負の無限大を表示する`() {
        showCarGapCard(AceWindowsGapData(0.0f, Float.NEGATIVE_INFINITY))

        rule.onNodeWithText("前車との差 0.0 秒").assertIsDisplayed()
        rule.onNodeWithText("後車との差 -- 秒").assertIsDisplayed()
    }

    @Test
    fun `未受信の場合は未取得を表示する`() {
        showCarGapCard(null)

        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    private fun showCarGapCard(gap: AceWindowsGapData?) {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState =
                        AceWindowsDebugStateDetailUiState(
                            cardOrder = listOf(AceWindowsDebugStateCardKey.CAR_GAP),
                            aceWindows = AceWindowsDebugState(gap = gap),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }
    }
}
