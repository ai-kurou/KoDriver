package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AceWindowsDebugStateDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイトルを表示して戻る操作を通知する`() {
        var backCount = 0
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState = AceWindowsDebugStateDetailUiState(),
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
                AceWindowsDebugStateDetailPaneContent(
                    uiState = AceWindowsDebugStateDetailUiState(),
                    canNavigateBack = false,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("デバッグ状態").assertIsDisplayed()
        rule.onNode(hasContentDescription("戻る")).assertDoesNotExist()
    }

    @Test
    fun `ACEの7枚だけを表示し未受信の6枚は未取得になる`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState = AceWindowsDebugStateDetailUiState(),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("Assetto Corsa EVO（Windows版）").assertIsDisplayed()
        listOf(
            "選択中のシミュレーター",
            "車両位置",
            "フラッグ情報",
            "並走車両",
            "自己ベストラップ",
            "タイヤカーカス温度",
            "燃料消費",
        ).forEach { title ->
            rule.onNodeWithText(title).assertIsDisplayed()
        }
        rule.onAllNodesWithText("未取得").assertCountEquals(6)
        rule.onNodeWithText("車両クラス").assertDoesNotExist()
    }

    @Test
    fun `cardOrderを入れ替えても両方のカードが指定順に表示される`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState =
                        AceWindowsDebugStateDetailUiState(
                            cardOrder =
                                listOf(
                                    AceWindowsDebugStateCardKey.FLAG_INFO,
                                    AceWindowsDebugStateCardKey.SIMULATOR,
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("フラッグ情報").assertIsDisplayed()
        rule.onNodeWithText("選択中のシミュレーター").assertIsDisplayed()
        val flag = rule.onNodeWithText("フラッグ情報").fetchSemanticsNode().boundsInRoot
        val simulator = rule.onNodeWithText("選択中のシミュレーター").fetchSemanticsNode().boundsInRoot
        assertTrue(flag.top < simulator.top || flag.left < simulator.left)
    }

    @Test
    fun `画面幅の境界に合わせて1列から3列に切り替える`() {
        assertEquals(1, calculateDebugStateColumns(399.dp))
        assertEquals(2, calculateDebugStateColumns(400.dp))
        assertEquals(2, calculateDebugStateColumns(699.dp))
        assertEquals(3, calculateDebugStateColumns(700.dp))
    }
}
