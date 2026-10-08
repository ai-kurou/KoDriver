package kurou.kodriver.core.designsystem

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ThresholdSliderTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `表示値の変更と操作完了をそれぞれ通知する`() {
        val changed = mutableListOf<Float>()
        val finished = mutableListOf<Float>()
        rule.setContent {
            KoDriverTheme {
                ThresholdSlider(
                    value = 3f,
                    valueRange = 1f..5f,
                    steps = 3,
                    labelFormatter = { "${it.toInt()}周" },
                    onValueChange = { changed += it },
                    onValueChangeFinished = { finished += it },
                )
            }
        }
        rule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(3f, 1f..5f, 3)))
            .performSemanticsAction(SemanticsActions.SetProgress) { it(5f) }
        rule.onNodeWithText("5周").assertExists()
        assertEquals(listOf(5f), changed)
        assertEquals(listOf(5f), finished)
    }

    @Test
    fun `変更通知を省略しても表示と操作完了は従来どおり更新する`() {
        val finished = mutableListOf<Float>()
        rule.setContent {
            KoDriverTheme {
                ThresholdSlider(
                    value = 3f,
                    valueRange = 1f..5f,
                    steps = 3,
                    labelFormatter = { "${it.toInt()}周" },
                    onValueChangeFinished = { finished += it },
                )
            }
        }
        rule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(3f, 1f..5f, 3)))
            .performSemanticsAction(SemanticsActions.SetProgress) { it(4f) }
        rule.onNodeWithText("4周").assertExists()
        assertEquals(listOf(4f), finished)
    }
}
