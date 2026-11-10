package kurou.kodriver.core.designsystem

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class KoDriverPhraseChipTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `渡した語句を表示する`() {
        rule.setContent { KoDriverTheme { KoDriverPhraseChip(text = "燃料") } }

        rule.onNodeWithText("燃料").assertIsDisplayed()
    }
}
