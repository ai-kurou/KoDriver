package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsClassCarGapData
import org.junit.Rule
import org.junit.Test

class DebugStateClassCarGapCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `クラス内順位と車両数を表示する`() {
        showClassCarGapCard(LmuWindowsClassCarGapData(1.0, 2.0, classPosition = 3, classVehicleCount = 20))

        rule.onNodeWithText("クラス内順位 3 / 20").assertIsDisplayed()
    }

    @Test
    fun `クラス内順位がnullの場合は欠損表示にする`() {
        showClassCarGapCard(LmuWindowsClassCarGapData(1.0, 2.0, classPosition = null, classVehicleCount = 20))

        rule.onNodeWithText("クラス内順位 -- / 20").assertIsDisplayed()
    }

    @Test
    fun `クラス内車両数がnullの場合は欠損表示にする`() {
        showClassCarGapCard(LmuWindowsClassCarGapData(1.0, 2.0, classPosition = 3, classVehicleCount = null))

        rule.onNodeWithText("クラス内順位 3 / --").assertIsDisplayed()
    }

    @Test
    fun `同クラスの前後差を小数1桁で表示する`() {
        showClassCarGapCard(LmuWindowsClassCarGapData(1.26, -2.34))

        rule.onNodeWithText("同クラス内順位と前後車両との時間差").assertIsDisplayed()
        rule.onNodeWithText("同クラスの前車との差 1.3 秒").assertIsDisplayed()
        rule.onNodeWithText("同クラスの後車との差 -2.3 秒").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertDoesNotExist()
    }

    @Test
    fun `前車がない場合は車両なし表示にする`() {
        showClassCarGapCard(LmuWindowsClassCarGapData(null, 0.0))

        rule.onNodeWithText("同クラス内順位と前後車両との時間差").assertIsDisplayed()
        rule.onNodeWithText("同クラスの前車との差 なし 秒").assertIsDisplayed()
        rule.onNodeWithText("同クラスの後車との差 0.0 秒").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertDoesNotExist()
    }

    @Test
    fun `後車がない場合は車両なし表示にする`() {
        showClassCarGapCard(LmuWindowsClassCarGapData(-0.0, null))

        rule.onNodeWithText("同クラス内順位と前後車両との時間差").assertIsDisplayed()
        rule.onNodeWithText("同クラスの前車との差 -0.0 秒").assertIsDisplayed()
        rule.onNodeWithText("同クラスの後車との差 なし 秒").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertDoesNotExist()
    }

    @Test
    fun `両方がnullでも取得済みの行を表示する`() {
        showClassCarGapCard(LmuWindowsClassCarGapData(null, null))

        rule.onNodeWithText("同クラス内順位と前後車両との時間差").assertIsDisplayed()
        rule.onNodeWithText("クラス内順位 -- / --").assertIsDisplayed()
        rule.onNodeWithText("同クラスの前車との差 なし 秒").assertIsDisplayed()
        rule.onNodeWithText("同クラスの後車との差 なし 秒").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertDoesNotExist()
    }

    @Test
    fun `非有限値は欠損表示にする`() {
        showClassCarGapCard(LmuWindowsClassCarGapData(Double.NaN, Double.POSITIVE_INFINITY))

        rule.onNodeWithText("同クラス内順位と前後車両との時間差").assertIsDisplayed()
        rule.onNodeWithText("同クラスの前車との差 -- 秒").assertIsDisplayed()
        rule.onNodeWithText("同クラスの後車との差 -- 秒").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertDoesNotExist()
    }

    @Test
    fun `未受信の場合は未取得を表示する`() {
        showClassCarGapCard(null)

        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    private fun showClassCarGapCard(classCarGap: LmuWindowsClassCarGapData?) {
        rule.setContent {
            MaterialTheme {
                LmuWindowsDebugStateDetailPaneContent(
                    uiState =
                        LmuWindowsDebugStateDetailUiState(
                            cardOrder = listOf(LmuWindowsDebugStateCardKey.CLASS_CAR_GAP),
                            lmuWindows = LmuWindowsDebugState(classCarGap = classCarGap),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }
    }
}
