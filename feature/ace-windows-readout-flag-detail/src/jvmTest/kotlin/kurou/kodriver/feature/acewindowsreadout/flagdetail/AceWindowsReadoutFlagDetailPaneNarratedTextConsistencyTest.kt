package kurou.kodriver.feature.acewindowsreadout.flagdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import kurou.kodriver.domain.engine.SpeechEvent
import org.junit.Rule
import org.junit.Test

/** 全10種の入力欄の既定文言がログ用既定文言と一致することを検証する。 */
class AceWindowsReadoutFlagDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `全10種の入力欄の既定値がSpeechEventのnarratedTextと一致する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsReadoutFlagDetailPaneContent()
            }
        }

        listOf(
            SpeechEvent.AceWindowsWhiteFlag().narratedText,
            SpeechEvent.AceWindowsGreenFlag().narratedText,
            SpeechEvent.AceWindowsRedFlag().narratedText,
            SpeechEvent.AceWindowsBlueFlag().narratedText,
            SpeechEvent.AceWindowsYellowFlag().narratedText,
            SpeechEvent.AceWindowsBlackFlag().narratedText,
            SpeechEvent.AceWindowsBlackWhiteFlag().narratedText,
            SpeechEvent.AceWindowsCheckeredFlag().narratedText,
            SpeechEvent.AceWindowsOrangeCircleFlag().narratedText,
            SpeechEvent.AceWindowsRedYellowStripesFlag().narratedText,
        ).forEachIndexed { index, narratedText ->
            rule
                .onAllNodesWithText(narratedText)[if (index < 8) 1 else 0]
                .assertTextContains(narratedText, substring = true)
        }
    }
}
