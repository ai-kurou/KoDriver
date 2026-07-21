package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DebugStateDetailPaneTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイトルを表示して戻る操作を通知する`() {
        var backCount = 0
        rule.setContent {
            MaterialTheme {
                DebugStateDetailPane(
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
    fun `幅400dp未満では列数は1`() {
        assertEquals(1, calculateDebugStateColumns(399.dp))
    }

    @Test
    fun `幅400dp以上700dp未満では列数は2`() {
        assertEquals(2, calculateDebugStateColumns(400.dp))
        assertEquals(2, calculateDebugStateColumns(699.dp))
    }

    @Test
    fun `幅700dp以上では列数は3`() {
        assertEquals(3, calculateDebugStateColumns(700.dp))
    }
}
