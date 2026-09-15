package kurou.kodriver.feature.acewindowsreadout.flagdetail

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
class AceWindowsReadoutFlagDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `Chip表示文言がSpeechEventのnarratedTextと一致する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutFlagDetailPaneContent()
            }
        }

        listOf(
            SpeechEvent.AceWindowsWhiteFlag.narratedText,
            SpeechEvent.AceWindowsGreenFlag.narratedText,
            SpeechEvent.AceWindowsRedFlag.narratedText,
            SpeechEvent.AceWindowsBlueFlag.narratedText,
            SpeechEvent.AceWindowsYellowFlag.narratedText,
            SpeechEvent.AceWindowsBlackFlag.narratedText,
            SpeechEvent.AceWindowsBlackWhiteFlag.narratedText,
            SpeechEvent.AceWindowsCheckeredFlag.narratedText,
            SpeechEvent.AceWindowsOrangeCircleFlag.narratedText,
            SpeechEvent.AceWindowsRedYellowStripesFlag.narratedText,
        ).forEach { narratedText ->
            rule.onAllNodesWithText(narratedText)[0].assertTextContains(narratedText, substring = true)
        }
    }
}
