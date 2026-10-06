package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DetailPaneCardChipRowTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `折り返したチップ行の下部にも8dpを確保する`() {
        rule.setContent {
            KoDriverTheme {
                Column {
                    DetailPaneCardChipRow(
                        chipLabels = listOf("フォーマルな口調", "カジュアルな口調"),
                        selectedChipLabels = setOf("フォーマルな口調"),
                        chipEnabled = true,
                        onChipClick = {},
                        modifier = Modifier.width(160.dp).testTag("chipRow"),
                    )
                    FlowRow(
                        modifier = Modifier.width(160.dp).testTag("baselineRow"),
                        horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
                        verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
                    ) {
                        DetailPaneCardChips(
                            chipLabels = listOf("フォーマルな口調", "カジュアルな口調"),
                            selectedChipLabels = setOf("フォーマルな口調"),
                            chipEnabled = true,
                            onChipClick = {},
                        )
                    }
                }
            }
        }

        val row = rule.onNodeWithTag("chipRow").fetchSemanticsNode().boundsInRoot
        val first = rule.onAllNodesWithText("フォーマルな口調")[0].fetchSemanticsNode().boundsInRoot
        val last = rule.onAllNodesWithText("カジュアルな口調")[0].fetchSemanticsNode().boundsInRoot
        assertTrue(last.top >= first.bottom)
        val expected = with(rule.density) { KoDriverSpacing.small.toPx() }
        val baseline = rule.onNodeWithTag("baselineRow").fetchSemanticsNode().boundsInRoot
        // Materialのチップ自身が持つタップ領域の余白と分けて、行への追加分を検証する。
        assertEquals(expected, row.height - baseline.height, absoluteTolerance = 1f)
    }

    @Test
    fun `チップの選択を通知し無効時はタップできない`() {
        var selected: String? = null
        rule.setContent {
            KoDriverTheme {
                Column {
                    DetailPaneCardChipRow(
                        chipLabels = listOf("試聴"),
                        selectedChipLabels = emptySet(),
                        chipEnabled = true,
                        onChipClick = { selected = it },
                    )
                    DetailPaneCardChipRow(
                        chipLabels = listOf("無効な試聴"),
                        selectedChipLabels = emptySet(),
                        chipEnabled = false,
                        onChipClick = { selected = it },
                    )
                }
            }
        }

        rule.onNodeWithText("試聴").performClick()
        assertEquals("試聴", selected)
        rule.onNodeWithText("無効な試聴").assertIsNotEnabled()
    }
}
