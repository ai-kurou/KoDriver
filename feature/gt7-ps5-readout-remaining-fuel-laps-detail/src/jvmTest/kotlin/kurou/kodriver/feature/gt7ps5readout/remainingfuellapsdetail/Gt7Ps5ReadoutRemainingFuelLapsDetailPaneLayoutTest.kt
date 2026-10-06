package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Gt7Ps5ReadoutRemainingFuelLapsDetailPaneLayoutTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `通常文言と燃料なし文言の間にボタン下部とグループの余白を確保する`() {
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                    uiState = Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(isTextToSpeechAvailable = true),
                    modifier = Modifier.requiredSize(360.dp, 4000.dp),
                )
            }
        }

        val emptyLabel = rule.onNodeWithText("燃料がありません(1.0周未満)のときの文言").fetchSemanticsNode().boundsInRoot
        val chip = rule.onNodeWithText("{laps}を挿入").fetchSemanticsNode().boundsInRoot
        val hint = rule.onNodeWithText("{laps} は残り周回数に置き換わります").fetchSemanticsNode().boundsInRoot
        val expected = with(rule.density) { (KoDriverSpacing.small + KoDriverSpacing.large).toPx() }
        assertTrue(emptyLabel.top - maxOf(chip.bottom, hint.bottom) >= expected)
    }

    @Test
    fun `タイトルと入力欄の間に4dpを確保する`() {
        rule.setContent {
            KoDriverTheme {
                Gt7Ps5ReadoutRemainingFuelLapsDetailPaneContent(
                    uiState = Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(isTextToSpeechAvailable = true),
                    modifier = Modifier.requiredSize(360.dp, 4000.dp),
                )
            }
        }

        val expected = with(rule.density) { KoDriverSpacing.extraSmall.toPx() }
        val fields = rule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().map { it.boundsInRoot }
        listOf(
            "1周以上のときの文言",
            "燃料がありません(1.0周未満)のときの文言",
        ).forEach { label ->
            rule.onAllNodesWithText(label).assertCountEquals(1)
            rule.onAllNodesWithText(label).fetchSemanticsNodes().forEach { node ->
                val labelBounds = node.boundsInRoot
                val fieldBounds = fields.first { it.top >= labelBounds.bottom }
                assertEquals(expected, fieldBounds.top - labelBounds.bottom, absoluteTolerance = 1f)
            }
        }
    }
}
