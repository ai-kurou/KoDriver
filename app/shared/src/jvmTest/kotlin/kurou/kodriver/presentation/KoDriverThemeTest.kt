package kurou.kodriver.presentation

import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.Test

class KoDriverThemeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `darkTheme=falseでクラッシュしない`() {
        composeRule.setContent {
            KoDriverTheme(darkTheme = false) {}
        }
    }

    @Test
    fun `darkTheme=trueでクラッシュしない`() {
        composeRule.setContent {
            KoDriverTheme(darkTheme = true) {}
        }
    }
}
