package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class Gt7Ps5DebugStateDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイトルを表示して戻る操作を通知する`() {
        var backCount = 0
        rule.setContent {
            MaterialTheme {
                Gt7Ps5DebugStateDetailPaneContent(
                    uiState = Gt7Ps5DebugStateDetailUiState(),
                    canNavigateBack = true,
                    onBack = { backCount++ },
                )
            }
        }

        rule.onNodeWithText("デバッグ状態").assertIsDisplayed()
        rule.onNode(hasContentDescription("戻る")).performClick()

        assertEquals(1, backCount)
    }

    @Test
    fun `戻れない場合は戻るボタンを表示しない`() {
        rule.setContent {
            MaterialTheme {
                Gt7Ps5DebugStateDetailPaneContent(
                    uiState = Gt7Ps5DebugStateDetailUiState(),
                    canNavigateBack = false,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("デバッグ状態").assertIsDisplayed()
        rule.onNode(hasContentDescription("戻る")).assertDoesNotExist()
    }

    @Test
    fun `未取得の5枚を含む全カードとグリッドを表示する`() {
        rule.setContent {
            MaterialTheme {
                Box(Modifier.requiredSize(900.dp, 600.dp)) {
                    Gt7Ps5DebugStateDetailPaneContent(
                        uiState = Gt7Ps5DebugStateDetailUiState(),
                        canNavigateBack = false,
                        onBack = {},
                    )
                }
            }
        }
        rule.onNodeWithTag(DEBUG_STATE_GRID_TEST_TAG).assertExists()
        rule.onAllNodesWithText("未取得").assertCountEquals(5)
        listOf("選択中のシミュレーター", "車両クラス", "現在のラップ", "自己ベストラップ", "タイヤ表面温度", "燃料消費")
            .forEach { rule.onNodeWithText(it).assertIsDisplayed() }
    }

    @Test
    fun `画面幅の境界で1から3列に切り替える`() {
        assertEquals(1, calculateDebugStateColumns(399.dp))
        assertEquals(2, calculateDebugStateColumns(400.dp))
        assertEquals(2, calculateDebugStateColumns(699.dp))
        assertEquals(3, calculateDebugStateColumns(700.dp))
    }
}
