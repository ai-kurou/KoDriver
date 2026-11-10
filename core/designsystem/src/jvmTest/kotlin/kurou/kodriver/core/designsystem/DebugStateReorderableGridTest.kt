package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private enum class SampleCardKey { FIRST, SECOND }

class DebugStateReorderableGridTest {
    private val moves = mutableListOf<Pair<Int, Int>>()
    private val onMoveCard: (Int, Int) -> Unit = { from, to -> moves.add(from to to) }

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `幅の境界に合わせて1列から3列に切り替える`() {
        assertEquals(1, calculateDebugStateColumns(399.dp))
        assertEquals(2, calculateDebugStateColumns(400.dp))
        assertEquals(2, calculateDebugStateColumns(699.dp))
        assertEquals(3, calculateDebugStateColumns(700.dp))
    }

    @Test
    fun `固定項目とカードをグリッド内に指定順で表示する`() {
        rule.setContent {
            KoDriverTheme {
                DebugStateReorderableGrid(
                    cardOrder = listOf(SampleCardKey.SECOND, SampleCardKey.FIRST),
                    fixedItemCount = 1,
                    onMoveCard = { _, _ -> },
                    fixedItems = {
                        item(key = "header", span = { GridItemSpan(maxLineSpan) }) { Text("固定ヘッダー") }
                    },
                    card = { cardKey, modifier -> Text(text = "カード ${cardKey.name}", modifier = modifier) },
                    modifier = Modifier.requiredSize(360.dp, 640.dp),
                )
            }
        }

        rule.onNodeWithTag(DEBUG_STATE_GRID_TEST_TAG).assertIsDisplayed()
        rule.onNodeWithText("固定ヘッダー").assertIsDisplayed()
        val header = rule.onNodeWithText("固定ヘッダー").fetchBounds()
        val second = rule.onNodeWithText("カード SECOND").fetchBounds()
        val first = rule.onNodeWithText("カード FIRST").fetchBounds()
        assertTrue(header.top < second.top)
        assertTrue(second.top < first.top)
    }

    @Test
    fun `カードを長押しで並べ替えると固定項目を除いたインデックスを通知する`() {
        rule.setContent {
            KoDriverTheme {
                DebugStateReorderableGrid(
                    cardOrder = listOf(SampleCardKey.FIRST, SampleCardKey.SECOND),
                    fixedItemCount = 1,
                    onMoveCard = onMoveCard,
                    fixedItems = {
                        item(key = "header", span = { GridItemSpan(maxLineSpan) }) { Text("固定ヘッダー") }
                    },
                    card = { cardKey, modifier -> Text(text = "カード ${cardKey.name}", modifier = modifier) },
                    modifier = Modifier.requiredSize(360.dp, 640.dp),
                )
            }
        }

        val grid = rule.onNodeWithTag(DEBUG_STATE_GRID_TEST_TAG)
        val gridTopLeft = grid.fetchSemanticsNode().boundsInRoot.topLeft
        val from = rule.onNodeWithText("カード FIRST").fetchBounds().center - gridTopLeft
        val to = rule.onNodeWithText("カード SECOND").fetchBounds().center - gridTopLeft
        grid.performTouchInput { down(from) }
        rule.mainClock.advanceTimeBy(1_000)
        grid.performTouchInput {
            repeat(10) { step -> moveTo(from + Offset(0f, (to.y - from.y) * (step + 1) / 10)) }
            up()
        }
        rule.waitForIdle()

        assertEquals(listOf(0 to 1), moves)
    }

    @Test
    fun `固定項目を移動先にした並べ替えは通知しない`() {
        rule.setContent {
            KoDriverTheme {
                DebugStateReorderableGrid(
                    cardOrder = listOf(SampleCardKey.FIRST, SampleCardKey.SECOND),
                    fixedItemCount = 1,
                    onMoveCard = onMoveCard,
                    fixedItems = {
                        item(key = "header", span = { GridItemSpan(maxLineSpan) }) { Text("固定ヘッダー") }
                    },
                    card = { cardKey, modifier -> Text(text = "カード ${cardKey.name}", modifier = modifier) },
                    modifier = Modifier.requiredSize(360.dp, 640.dp),
                )
            }
        }

        val grid = rule.onNodeWithTag(DEBUG_STATE_GRID_TEST_TAG)
        val gridTopLeft = grid.fetchSemanticsNode().boundsInRoot.topLeft
        val from = rule.onNodeWithText("カード FIRST").fetchBounds().center - gridTopLeft
        val to = rule.onNodeWithText("固定ヘッダー").fetchBounds().center - gridTopLeft
        grid.performTouchInput { down(from) }
        rule.mainClock.advanceTimeBy(1_000)
        grid.performTouchInput {
            repeat(10) { step -> moveTo(from + Offset(0f, (to.y - from.y) * (step + 1) / 10)) }
            up()
        }
        rule.waitForIdle()

        assertTrue(moves.isEmpty())
    }

    private fun androidx.compose.ui.test.SemanticsNodeInteraction.fetchBounds() = fetchSemanticsNode().boundsInRoot
}
