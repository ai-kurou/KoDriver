package kurou.kodriver.feature.acewindowsreadout.flagdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import kurou.kodriver.domain.engine.SpeechEvent
import org.junit.Rule
import org.junit.Test

/** 8種の入力欄の既定文言と残り2種のチップ文言がログ用既定文言と一致することを検証する。 */
class AceWindowsReadoutFlagDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `入力欄の既定値とWAVチップがSpeechEventのnarratedTextと一致する`() {
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
            rule.onAllNodesWithText(narratedText)[1].assertTextContains(narratedText, substring = true)
        }
    }
}
