package kurou.kodriver

import androidx.compose.ui.test.junit4.v2.createComposeRule
import kurou.kodriver.presentation.AppScreen
import org.junit.Rule
import org.junit.Test

class AppStartupTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `アプリ画面が起動する`() {
        rule.setContent {
            AppScreen(readoutContent = {})
        }
        rule.waitForIdle()
    }
}
