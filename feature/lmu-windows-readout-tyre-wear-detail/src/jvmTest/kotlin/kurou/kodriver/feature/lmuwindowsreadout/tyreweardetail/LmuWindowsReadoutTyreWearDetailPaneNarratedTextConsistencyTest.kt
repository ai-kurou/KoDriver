package kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail

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
class LmuWindowsReadoutTyreWearDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `Chip表示文言がSpeechEventのnarratedTextと一致する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutTyreWearDetailPaneContent()
            }
        }

        val narratedText = SpeechEvent.TyreWearWarning.narratedText
        rule.onAllNodesWithText(narratedText)[0].assertTextContains(narratedText, substring = true)
    }
}
