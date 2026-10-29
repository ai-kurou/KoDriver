package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.acewindows.model.AceWindowsGapData
import kurou.kodriver.domain.acewindows.model.AceWindowsOverallPositionData
import org.junit.Rule
import org.junit.Test

class DebugStateCarGapCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `前後車両との差を符号を保持して表示する`() {
        showCarGapCard(AceWindowsGapData(1.0f, -2.34f), AceWindowsOverallPositionData(3, 20))

        rule.onNodeWithText("総合順位 3 / 20").assertIsDisplayed()

        rule.onNodeWithText("総合順位と前後車両との時間差").assertIsDisplayed()
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

    @Test
    fun `未確定の順位と総台数は欠損表示にする`() {
        showCarGapCard(AceWindowsGapData(1.0f, 2.0f), AceWindowsOverallPositionData(0, 0))

        rule.onNodeWithText("総合順位 -- / --").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertDoesNotExist()
    }

    @Test
    fun `順位だけ未受信なら順位を欠損表示にする`() {
        showCarGapCard(AceWindowsGapData(1.0f, 2.0f))

        rule.onNodeWithText("総合順位 -- / --").assertIsDisplayed()
        rule.onNodeWithText("前車との差 1.0 秒").assertIsDisplayed()
        rule.onNodeWithText("後車との差 2.0 秒").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertDoesNotExist()
    }

    @Test
    fun `ギャップだけ未受信なら時間差を欠損表示にする`() {
        showCarGapCard(null, AceWindowsOverallPositionData(3, 20))

        rule.onNodeWithText("総合順位 3 / 20").assertIsDisplayed()
        rule.onNodeWithText("前車との差 -- 秒").assertIsDisplayed()
        rule.onNodeWithText("後車との差 -- 秒").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertDoesNotExist()
    }

    private fun showCarGapCard(
        gap: AceWindowsGapData?,
        overallPosition: AceWindowsOverallPositionData? = null,
    ) {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState =
                        AceWindowsDebugStateDetailUiState(
                            cardOrder = listOf(AceWindowsDebugStateCardKey.CAR_GAP),
                            aceWindows = AceWindowsDebugState(gap = gap, overallPosition = overallPosition),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }
    }
}
