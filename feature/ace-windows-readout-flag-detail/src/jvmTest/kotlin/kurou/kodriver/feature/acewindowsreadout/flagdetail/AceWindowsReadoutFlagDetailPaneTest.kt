package kurou.kodriver.feature.acewindowsreadout.flagdetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test

class AceWindowsReadoutFlagDetailPaneTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイトルと説明文が表示される`() {
        rule.setContent {
            KoDriverTheme {
                AceWindowsReadoutFlagDetailPaneContent()
            }
        }

        rule.onNodeWithText("フラッグ").assertIsDisplayed()
        rule.onNodeWithText(
            "ブルーフラッグ・イエローフラッグ・レッドフラッグ・フルコースイエローなどのフラッグ状況を音声でお知らせします。",
        ).assertIsDisplayed()
    }
}
