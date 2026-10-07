package kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail

import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsReadoutTyreTemperatureDetailUiStateTest {
    @Test
    fun `初期状態はドメインの既定文言と閾値を使う`() {
        val state = AceWindowsReadoutTyreTemperatureDetailUiState()
        assertEquals(ACE_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT, state.overheatReadoutText)
        assertEquals(ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value, state.highThresholdCelsius)
        assertEquals(true, state.overheatWarningEnabled)
        assertEquals(false, state.isTextToSpeechAvailable)
    }
}
