package kurou.kodriver.core.designsystem

import androidx.compose.ui.graphics.Color
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

    @Test
    fun `アクセント色を渡しても語句を表示する`() {
        rule.setContent { KoDriverTheme { KoDriverPhraseChip(text = "タイヤ", accent = Color.Red) } }

        rule.onNodeWithText("タイヤ").assertIsDisplayed()
    }

    @Test
    fun `控えめ表示でも語句を表示する`() {
        rule.setContent { KoDriverTheme { KoDriverPhraseChip(text = "？", muted = true) } }

        rule.onNodeWithText("？").assertIsDisplayed()
    }

    @Test
    fun `強調してもアクセント色の有無によらず語句を表示する`() {
        rule.setContent {
            KoDriverTheme {
                KoDriverPhraseChip(text = "強調あり", accent = Color.Red, emphasis = 1f)
                KoDriverPhraseChip(text = "色なし", emphasis = 0.5f)
            }
        }

        rule.onNodeWithText("強調あり").assertIsDisplayed()
        rule.onNodeWithText("色なし").assertIsDisplayed()
    }
}
