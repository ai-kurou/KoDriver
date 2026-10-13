package kurou.kodriver.feature.lmuwindowsautoreadout.vehicleapproachdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import kurou.kodriver.domain.engine.LmuWindowsCarLeft
import kurou.kodriver.domain.engine.LmuWindowsCarLeftSustained
import kurou.kodriver.domain.engine.LmuWindowsCarRight
import kurou.kodriver.domain.engine.LmuWindowsCarRightSustained
import kurou.kodriver.domain.engine.SpeechEvent
import org.junit.Rule
import org.junit.Test

/**
 * 開始時・継続時の既定文言が表示されることを確認する。
 * [SpeechEvent.narratedText] を実際の表示テキストと突き合わせる（#1527）。
 */
class LmuWindowsAutoReadoutVehicleApproachDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `表示文言がSpeechEventのnarratedTextを含む`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsAutoReadoutVehicleApproachDetailPaneContent(
                    uiState = LmuWindowsAutoReadoutVehicleApproachDetailUiState(isTextToSpeechAvailable = true),
                )
            }
        }

        listOf(
            LmuWindowsCarLeft().narratedText,
            LmuWindowsCarRight().narratedText,
        ).forEach { narratedText ->
            rule.onNode(hasSetTextAction() and hasText(narratedText)).assertTextContains(narratedText)
        }
        listOf(
            LmuWindowsCarRightSustained().narratedText,
            LmuWindowsCarLeftSustained().narratedText,
        ).forEach { narratedText ->
            rule.onNode(hasSetTextAction() and hasText(narratedText)).assertTextContains(narratedText)
        }
    }
}
