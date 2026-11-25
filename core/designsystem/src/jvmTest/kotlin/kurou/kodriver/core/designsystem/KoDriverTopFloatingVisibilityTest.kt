package kurou.kodriver.core.designsystem

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

class KoDriverTopFloatingVisibilityTest {
    @get:Rule
    val rule = createComposeRule()

    private var visible by mutableStateOf(false)

    private fun setContent() {
        rule.setContent {
            KoDriverTopFloatingVisibility(visible = visible) {
                Text("content")
            }
        }
    }

    @Test
    fun `表示に切り替えると上からスライドして定位置に収まる`() {
        setContent()
        rule.waitForIdle()
        rule.onNodeWithText("content").assertDoesNotExist()
        rule.mainClock.autoAdvance = false

        visible = true
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeByFrame()
        val enteringTop = top()
        rule.mainClock.advanceTimeBy(1_000)
        val shownTop = top()

        assertTrue(enteringTop < shownTop, "entering=$enteringTop shown=$shownTop")
    }

    @Test
    fun `非表示に切り替えると上へスライドしてから取り除かれる`() {
        visible = true
        setContent()
        rule.waitForIdle()
        val shownTop = top()
        rule.mainClock.autoAdvance = false

        visible = false
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(100)
        val exitingTop = top()
        rule.mainClock.advanceTimeBy(1_000)

        assertTrue(exitingTop < shownTop, "exiting=$exitingTop shown=$shownTop")
        rule.onNodeWithText("content").assertDoesNotExist()
    }

    private fun top(): Float =
        rule
            .onNodeWithText("content")
            .fetchSemanticsNode()
            .positionInRoot.y
}
