package kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.engine.LmuWindowsBrakeOverheat
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsBrakeTemperatureReadoutText
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsAutoReadoutBrakeTemperatureDetailPaneNarratedTextConsistencyTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `既定文言の入力欄とイベントの閾値置換が一致する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsAutoReadoutBrakeTemperatureDetailPaneContent(
                    uiState = LmuWindowsAutoReadoutBrakeTemperatureDetailUiState(isTextToSpeechAvailable = true),
                )
            }
        }
        rule.onNode(hasSetTextAction()).assertTextContains(LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT)
        assertEquals(
            formatLmuWindowsBrakeTemperatureReadoutText(LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT, 800),
            LmuWindowsBrakeOverheat(800).narratedText,
        )
    }
}
