package kurou.kodriver.feature.lmuwindowsreadout.braketemperaturedetail

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.formatLmuWindowsBrakeTemperatureReadoutText
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutBrakeTemperatureDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `既定文言の入力欄とイベントの閾値置換が一致する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeTemperatureDetailPaneContent(
                    uiState = LmuWindowsReadoutBrakeTemperatureDetailUiState(isTextToSpeechAvailable = true),
                )
            }
        }
        rule.onNode(hasSetTextAction()).assertTextContains(LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT)
        assertEquals(
            formatLmuWindowsBrakeTemperatureReadoutText(LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT, 800),
            SpeechEvent.BrakeOverheat(800).narratedText,
        )
    }
}
