package kurou.kodriver.feature.lmuwindowsautoreadout.flagdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import kurou.kodriver.domain.engine.LmuWindowsBlueFlag
import kurou.kodriver.domain.engine.LmuWindowsFullCourseYellow
import kurou.kodriver.domain.engine.LmuWindowsRedFlag
import kurou.kodriver.domain.engine.LmuWindowsYellowFlag
import kurou.kodriver.domain.engine.SpeechEvent
import org.junit.Rule
import org.junit.Test

/** フラッグの既定文言と、ログ用の既定文言を表す [SpeechEvent.narratedText] の一致を検証する。 */
class LmuWindowsAutoReadoutFlagDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `フラッグの既定文言がSpeechEventのnarratedTextと一致する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsAutoReadoutFlagDetailPaneContent(
                    uiState = LmuWindowsAutoReadoutFlagDetailUiState(),
                    onFlagEnabledChanged = { _, _ -> },
                    onFlagTextChanged = { _, _ -> },
                    onFlagTextReset = {},
                    onFlagTextPreviewClicked = {},
                )
            }
        }

        listOf(
            LmuWindowsBlueFlag().narratedText,
            LmuWindowsYellowFlag().narratedText,
            LmuWindowsFullCourseYellow().narratedText,
            LmuWindowsRedFlag().narratedText,
        ).forEach { narratedText ->
            rule.onAllNodesWithText(narratedText)[0].assertTextContains(narratedText, substring = true)
        }
    }
}
