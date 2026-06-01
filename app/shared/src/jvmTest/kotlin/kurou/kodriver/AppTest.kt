package kurou.kodriver

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class AppTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `ボタンクリックで Compose テキストが表示され再クリックで非表示になる`() {
        rule.setContent { App() }

        rule.onNodeWithText("Click me!").assertExists()

        rule.onNodeWithText("Click me!").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Compose:", substring = true).assertExists()

        rule.onNodeWithText("Click me!").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Compose:", substring = true).assertDoesNotExist()
    }
}
