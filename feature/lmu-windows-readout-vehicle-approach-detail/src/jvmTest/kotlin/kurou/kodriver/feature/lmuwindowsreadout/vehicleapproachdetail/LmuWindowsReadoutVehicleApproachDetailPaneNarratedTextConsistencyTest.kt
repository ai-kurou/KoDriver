package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

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
 * このモジュールのChipは「カーレフト・カーライト」のように左右2種のnarratedTextを
 * 1つの文言に連結して表示するため、完全一致ではなく部分一致で検証する。
 */
class LmuWindowsReadoutVehicleApproachDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `Chip表示文言がSpeechEventのnarratedTextを含む`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleApproachDetailUiState(),
                )
            }
        }

        listOf(
            SpeechEvent.CarLeft.narratedText,
            SpeechEvent.CarRight.narratedText,
            SpeechEvent.LeftApproach.narratedText,
            SpeechEvent.RightApproach.narratedText,
            SpeechEvent.KeepLeft.narratedText,
            SpeechEvent.KeepRight.narratedText,
            SpeechEvent.LeftSustained.narratedText,
            SpeechEvent.RightSustained.narratedText,
        ).forEach { narratedText ->
            rule
                .onAllNodesWithText(narratedText, substring = true)[0]
                .assertTextContains(narratedText, substring = true)
        }
    }
}
