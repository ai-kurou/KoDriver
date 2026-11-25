package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KoDriverCollapsibleVisibilityTest {
    @get:Rule
    val rule = createComposeRule()

    private var visible by mutableStateOf(false)

    private fun setContent() {
        rule.setContent {
            Column(verticalArrangement = Arrangement.spacedBy(40.dp)) {
                Text("above")
                KoDriverCollapsibleVisibility(visible = visible) {
                    Text("content")
                }
                Text("below")
            }
        }
    }

    @Test
    fun `表示に切り替えると下の要素が少しずつ押し下げられる`() {
        setContent()
        rule.waitForIdle()
        val hiddenTop = belowTop()
        rule.mainClock.autoAdvance = false

        visible = true
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(100)
        val expandingTop = belowTop()
        rule.mainClock.advanceTimeBy(1_000)
        val shownTop = belowTop()

        rule.onNodeWithText("content").assertExists()
        assertTrue(expandingTop < shownTop, "hidden=$hiddenTop expanding=$expandingTop shown=$shownTop")
    }

    @Test
    fun `非表示に切り替えると消えるまで中身を残し完了後は子要素の間隔も残さない`() {
        setContent()
        rule.waitForIdle()
        val hiddenTop = belowTop()
        visible = true
        rule.waitForIdle()
        rule.mainClock.autoAdvance = false

        visible = false
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithText("content").assertExists()
        rule.mainClock.advanceTimeBy(1_000)

        rule.onNodeWithText("content").assertDoesNotExist()
        assertEquals(hiddenTop, belowTop())
    }

    private fun belowTop(): Float =
        rule
            .onNodeWithText("below")
            .fetchSemanticsNode()
            .positionInRoot.y
}
