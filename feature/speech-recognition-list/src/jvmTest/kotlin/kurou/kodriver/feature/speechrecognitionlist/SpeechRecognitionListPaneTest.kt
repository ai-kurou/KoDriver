package kurou.kodriver.feature.speechrecognitionlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test

class SpeechRecognitionListPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `空の一覧ペインを表示する`() {
        rule.setContent { KoDriverTheme { SpeechRecognitionListPane() } }

        rule.onNodeWithTag(SPEECH_RECOGNITION_LIST_PANE_TEST_TAG).assertIsDisplayed()
    }
}
