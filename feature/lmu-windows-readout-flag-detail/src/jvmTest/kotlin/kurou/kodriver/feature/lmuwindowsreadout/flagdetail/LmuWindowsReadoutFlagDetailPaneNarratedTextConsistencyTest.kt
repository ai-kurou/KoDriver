package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import kurou.kodriver.domain.engine.SpeechEvent
import org.junit.Rule
import org.junit.Test

/** フラッグの既定文言と、ログ用の既定文言を表す [SpeechEvent.narratedText] の一致を検証する。 */
class LmuWindowsReadoutFlagDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `フラッグの既定文言がSpeechEventのnarratedTextと一致する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState = LmuWindowsReadoutFlagDetailUiState(),
                    onFlagEnabledChanged = { _, _ -> },
                    onFlagTextChanged = { _, _ -> },
                    onFlagTextPreviewClicked = {},
                )
            }
        }

        listOf(
            SpeechEvent.BlueFlag.narratedText,
            SpeechEvent.YellowFlag.narratedText,
            SpeechEvent.FullCourseYellow.narratedText,
            SpeechEvent.RedFlag.narratedText,
        ).forEach { narratedText ->
            rule.onAllNodesWithText(narratedText)[0].assertTextContains(narratedText, substring = true)
        }
    }
}
