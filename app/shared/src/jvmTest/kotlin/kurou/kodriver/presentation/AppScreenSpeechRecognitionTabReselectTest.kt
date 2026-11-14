package kurou.kodriver.presentation

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class AppScreenSpeechRecognitionTabReselectTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `音声タブを再タップするとonSpeechRecognitionTabReselectedが呼ばれる`() {
        var speechReselectedCount = 0
        var otherReselectedCount = 0

        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                onSpeechRecognitionTabReselected = { speechReselectedCount++ },
                onOtherTabReselected = { otherReselectedCount++ },
            )
        }

        rule.onNode(hasText("音声")).performClick()
        rule.waitForIdle()
        assertEquals(0, speechReselectedCount)

        rule.onNode(hasText("音声")).performClick()
        rule.waitForIdle()

        assertEquals(1, speechReselectedCount)
        assertEquals(0, otherReselectedCount)
    }

    @Test
    fun `音声タブにはscrollToTopRequestが渡される`() {
        var received = -1

        rule.setContent {
            AppScreenContent(
                layoutType = NavigationSuiteType.NavigationBar,
                speechRecognitionScrollToTopRequest = 3,
                speechRecognitionContent = {
                    received = it
                    Text("音声画面")
                },
            )
        }

        rule.onNode(hasText("音声")).performClick()
        rule.waitForIdle()

        assertEquals(3, received)
    }
}
