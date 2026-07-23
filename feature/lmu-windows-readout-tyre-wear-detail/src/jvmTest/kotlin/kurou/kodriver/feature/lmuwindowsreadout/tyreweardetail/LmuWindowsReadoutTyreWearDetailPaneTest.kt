package kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutTyreWearDetailPaneTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `説明文が表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreWearDetailPaneContent()
            }
        }

        rule.onNodeWithText("タイヤの摩耗状況を音声でお知らせします。").assertIsDisplayed()
    }

    @Test
    fun `摩耗警告カードとデフォルトONのタイヤ摩耗警告チップが表示される`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreWearDetailPaneContent()
            }
        }

        rule.onNodeWithText("摩耗警告").assertIsDisplayed()
        rule.onNodeWithText("タイヤ摩耗警告")
            .assertIsDisplayed()
            .assertIsSelected()
    }

    @Test
    fun `タイヤ摩耗警告チップをタップするとonWarningChipClickedが呼ばれる`() {
        var clicked = false
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutTyreWearDetailPaneContent(
                    onWarningChipClicked = { clicked = true },
                )
            }
        }

        rule.onNodeWithText("タイヤ摩耗警告").performClick()

        assertEquals(true, clicked)
    }
}
