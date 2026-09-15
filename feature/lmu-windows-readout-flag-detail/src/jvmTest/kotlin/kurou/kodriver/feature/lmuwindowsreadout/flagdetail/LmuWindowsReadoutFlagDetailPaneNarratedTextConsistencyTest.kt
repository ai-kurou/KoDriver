package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import kurou.kodriver.domain.engine.SpeechEvent
import org.junit.Rule
import org.junit.Test

/**
 * [SpeechEvent.narratedText] は WAV と同じ内容を Chip 表示文言から複製したものであるため、
 * Chip側の文言変更を検知できるよう実際の表示テキストと突き合わせる（#1527）。
 */
class LmuWindowsReadoutFlagDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `Chip表示文言がSpeechEventのnarratedTextと一致する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutFlagDetailPaneContent(
                    uiState = LmuWindowsReadoutFlagDetailUiState(),
                    onFlagEnabledChanged = { _, _ -> },
                    onPreviewClicked = {},
                    onRedFlagEnabledChanged = {},
                    onRedFlagVoiceTypeChanged = {},
                    onRedFlagPreviewClicked = {},
                )
            }
        }

        listOf(
            SpeechEvent.BlueFlag.narratedText,
            SpeechEvent.YellowFlag.narratedText,
            SpeechEvent.FullCourseYellow.narratedText,
            SpeechEvent.RedFlag.narratedText,
            SpeechEvent.SessionStop.narratedText,
        ).forEach { narratedText ->
            rule.onAllNodesWithText(narratedText)[0].assertTextContains(narratedText, substring = true)
        }
    }
}
