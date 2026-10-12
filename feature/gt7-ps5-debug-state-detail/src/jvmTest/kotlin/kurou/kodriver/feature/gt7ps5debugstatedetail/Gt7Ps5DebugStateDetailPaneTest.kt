package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
                Box(Modifier.requiredSize(1560.dp, 1080.dp)) {
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

    @Test
    fun `cardOrderを入れ替えても両方のカードが指定順に表示される`() {
        rule.setContent {
            MaterialTheme {
                Gt7Ps5DebugStateDetailPaneContent(
                    uiState =
                        Gt7Ps5DebugStateDetailUiState(
                            cardOrder =
                                listOf(
                                    Gt7Ps5DebugStateCardKey.VEHICLE_CLASS,
                                    Gt7Ps5DebugStateCardKey.SIMULATOR,
                                ),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("車両クラス").assertIsDisplayed()
        rule.onNodeWithText("選択中のシミュレーター").assertIsDisplayed()
        val flag = rule.onNodeWithText("車両クラス").fetchSemanticsNode().boundsInRoot
        val simulator = rule.onNodeWithText("選択中のシミュレーター").fetchSemanticsNode().boundsInRoot
        val input = rule.onNodeWithText("オーバーレイ表示テスト").fetchSemanticsNode().boundsInRoot
        assertTrue(input.top < simulator.top)
        assertTrue(flag.top < simulator.top || flag.left < simulator.left)
    }

    @Test
    fun `長押しドラッグで移動元と移動先を通知して表示順を更新する`() {
        val order = listOf(Gt7Ps5DebugStateCardKey.SIMULATOR, Gt7Ps5DebugStateCardKey.VEHICLE_CLASS)
        val state = mutableStateOf(Gt7Ps5DebugStateDetailUiState(cardOrder = order))
        val moves = mutableListOf<Pair<Int, Int>>()
        rule.setContent {
            MaterialTheme {
                Gt7Ps5DebugStateDetailPaneContent(
                    uiState = state.value,
                    canNavigateBack = false,
                    onBack = {},
                    onMoveCard = { from, to ->
                        moves.add(from to to)
                        state.value = state.value.copy(cardOrder = order.reversed())
                    },
                    modifier = Modifier.requiredSize(360.dp, 1080.dp),
                )
            }
        }
        val grid = rule.onNodeWithTag(DEBUG_STATE_GRID_TEST_TAG)
        val origin = grid.fetchSemanticsNode().boundsInRoot.topLeft
        val from =
            rule
                .onNodeWithText("選択中のシミュレーター")
                .fetchSemanticsNode()
                .boundsInRoot.center - origin
        val to =
            rule
                .onNodeWithText("車両クラス")
                .fetchSemanticsNode()
                .boundsInRoot.center - origin
        grid.performTouchInput { down(from) }
        rule.mainClock.advanceTimeBy(1_000)
        grid.performTouchInput {
            repeat(10) { step ->
                moveTo(from + Offset(0f, (to.y - from.y) * (step + 1) / 10))
            }
            up()
        }
        rule.waitForIdle()
        assertEquals(listOf(0 to 1), moves)
        val vehicleClass = rule.onNodeWithText("車両クラス").fetchSemanticsNode().boundsInRoot
        val simulator = rule.onNodeWithText("選択中のシミュレーター").fetchSemanticsNode().boundsInRoot
        val input = rule.onNodeWithText("オーバーレイ表示テスト").fetchSemanticsNode().boundsInRoot
        assertTrue(input.top < simulator.top)
        assertTrue(vehicleClass.top < simulator.top)
    }

    @Test
    fun `先頭の入力カードは入力と選択と送信を通知する`() {
        val state = mutableStateOf(Gt7Ps5DebugStateDetailUiState(cardOrder = emptyList()))
        var sent = 0
        rule.setContent {
            MaterialTheme {
                Gt7Ps5DebugStateDetailPaneContent(
                    uiState = state.value,
                    canNavigateBack = false,
                    onBack = {},
                    onDebugOverlayTextChange = { state.value = state.value.copy(debugOverlayText = it) },
                    onDebugOverlayKeySelect = { state.value = state.value.copy(debugOverlaySelectedKey = it) },
                    onSendDebugOverlayText = { sent++ },
                )
            }
        }
        rule.onNodeWithText("オーバーレイ表示テスト").assertIsDisplayed()
        rule.onNodeWithText("送信").assertIsNotEnabled()
        rule.onNodeWithText("表示する文字列").performTextInput("表示テスト")
        rule.onNodeWithText("送信").assertIsEnabled().performClick()
        assertEquals("表示テスト", state.value.debugOverlayText)
        assertEquals(1, sent)
        val keys = ReadoutItemKey.entriesOf(Simulator.Gt7Ps5)
        val initial = keys.first().value
        val selected = keys[1].value
        rule.onNodeWithText(" ($initial)", substring = true).performClick()
        rule.onNodeWithText(" ($selected)", substring = true).performClick()
        assertEquals(keys[1], state.value.debugOverlaySelectedKey)
    }
}
