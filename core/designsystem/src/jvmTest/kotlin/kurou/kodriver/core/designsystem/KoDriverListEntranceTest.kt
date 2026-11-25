package kurou.kodriver.core.designsystem

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

class KoDriverListEntranceTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `表示直後は下にずれていて上へ移動しながら定位置に収まる`() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            KoDriverListEntrance {
                Text("content")
            }
        }
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeByFrame()
        val enteringTop = top()

        rule.mainClock.advanceTimeBy(1_000)
        val settledTop = top()

        assertTrue(enteringTop > settledTop, "entering=$enteringTop settled=$settledTop")
    }

    private fun top(): Float =
        rule
            .onNodeWithText("content")
            .fetchSemanticsNode()
            .positionInRoot.y
}
