package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsCarGapData
import org.junit.Rule
import org.junit.Test

class DebugStateCarGapCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `全体ギャップと参考用の生値を表示する`() {
        showCarGapCard(LmuWindowsCarGapData(1.0, -2.34, 0.0, 4.56, 1.26, 9.84))

        rule.onNodeWithText("前後車両との時間差（全クラス）").assertIsDisplayed()
        rule.onNodeWithText("全体の前車との差 1.3 秒").assertIsDisplayed()
        rule.onNodeWithText("全体の後車との差 9.8 秒").assertIsDisplayed()
        rule.onNodeWithText("前車との差 1.0 秒").assertIsDisplayed()
        rule.onNodeWithText("後車との差 -2.3 秒").assertIsDisplayed()
        rule.onNodeWithText("順位上の前車との差 0.0 秒").assertIsDisplayed()
        rule.onNodeWithText("順位上の後車との差 4.6 秒").assertIsDisplayed()
    }

    @Test
    fun `非有限値は欠損表示にする`() {
        showCarGapCard(LmuWindowsCarGapData(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 1.0))

        rule.onNodeWithText("前車との差 -- 秒").assertIsDisplayed()
        rule.onNodeWithText("後車との差 -- 秒").assertIsDisplayed()
        rule.onNodeWithText("順位上の前車との差 -- 秒").assertIsDisplayed()
        rule.onNodeWithText("順位上の後車との差 1.0 秒").assertIsDisplayed()
    }

    @Test
    fun `全体ギャップの車両なしと非有限値を区別する`() {
        showCarGapCard(LmuWindowsCarGapData(1.0, 2.0, 3.0, 4.0, null, Double.NaN))

        rule.onNodeWithText("全体の前車との差 なし 秒").assertIsDisplayed()
        rule.onNodeWithText("全体の後車との差 -- 秒").assertIsDisplayed()
    }

    @Test
    fun `未受信の場合は未取得を表示する`() {
        showCarGapCard(null)

        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    private fun showCarGapCard(carGap: LmuWindowsCarGapData?) {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.CAR_GAP),
                            lmuWindows = LmuWindowsDebugState(carGap = carGap),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }
    }
}
