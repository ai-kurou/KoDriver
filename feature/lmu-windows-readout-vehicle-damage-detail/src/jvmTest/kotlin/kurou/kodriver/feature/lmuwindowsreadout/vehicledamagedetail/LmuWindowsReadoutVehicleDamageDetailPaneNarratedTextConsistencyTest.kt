package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import kurou.kodriver.domain.engine.SpeechEvent
import org.junit.Rule
import org.junit.Test

/** 既定文言とイベントの narratedText が一致することを確認する。 */
class LmuWindowsReadoutVehicleDamageDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `入力欄の既定文言がSpeechEventのnarratedTextと一致する`() {
        rule.setContent {
            MaterialTheme {
                LmuWindowsReadoutVehicleDamageDetailPaneContent(
                    uiState = LmuWindowsReadoutVehicleDamageDetailUiState(isTextToSpeechAvailable = true),
                )
            }
        }

        listOf(
            SpeechEvent.LmuWindowsOverheating().narratedText,
            SpeechEvent.LmuWindowsPartDetached().narratedText,
            SpeechEvent.LmuWindowsTyreDetached().narratedText,
        ).forEach { narratedText ->
            rule.onNode(hasSetTextAction() and hasText(narratedText)).assertTextContains(narratedText, substring = true)
        }
    }
}
