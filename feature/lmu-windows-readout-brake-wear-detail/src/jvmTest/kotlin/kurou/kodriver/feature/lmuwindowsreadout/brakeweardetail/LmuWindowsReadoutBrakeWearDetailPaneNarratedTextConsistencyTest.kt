package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.formatLmuWindowsBrakeWearReadoutText
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutBrakeWearDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `既定文言の入力欄とイベントの閾値置換が一致する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent(
                    uiState = LmuWindowsReadoutBrakeWearDetailUiState(isTextToSpeechAvailable = true),
                )
            }
        }
        rule.onNode(hasSetTextAction()).assertTextContains(LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT)
        assertEquals(
            formatLmuWindowsBrakeWearReadoutText(LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT, 20),
            SpeechEvent.LmuWindowsBrakeWearLow(20).narratedText,
        )
    }
}
