package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import kurou.kodriver.domain.engine.SpeechEvent
import org.junit.Rule
import org.junit.Test

/**
 * 開始時・継続時の既定文言が表示されることを確認する。
 * [SpeechEvent.narratedText] を実際の表示テキストと突き合わせる（#1527）。
 * 継続時の暫定表示は左右2種の文言を連結して表示するため、部分一致で検証する。
 */
class LmuWindowsReadoutVehicleApproachDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `表示文言がSpeechEventのnarratedTextを含む`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutVehicleApproachDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleApproachDetailUiState(isTextToSpeechAvailable = true),
                )
            }
        }

        listOf(SpeechEvent.CarLeft.narratedText, SpeechEvent.CarRight.narratedText).forEach { narratedText ->
            rule.onNode(hasSetTextAction() and hasText(narratedText)).assertTextContains(narratedText)
        }
        listOf(
            SpeechEvent.CarRightSustained.narratedText,
            SpeechEvent.CarLeftSustained.narratedText,
        ).forEach { narratedText ->
            rule
                .onAllNodesWithText(narratedText, substring = true)[0]
                .assertTextContains(narratedText, substring = true)
        }
    }
}
