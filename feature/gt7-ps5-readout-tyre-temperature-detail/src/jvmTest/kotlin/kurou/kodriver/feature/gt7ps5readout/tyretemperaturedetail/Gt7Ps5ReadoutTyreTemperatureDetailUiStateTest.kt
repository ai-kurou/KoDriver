package kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail

import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5ReadoutTyreTemperatureDetailUiStateTest {
    @Test
    fun `初期状態はドメインの既定値を使用しTTSは利用不可`() {
        val state = Gt7Ps5ReadoutTyreTemperatureDetailUiState()
        assertEquals(GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT, state.readoutText)
        assertEquals(GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value, state.highThresholdCelsius)
        assertEquals(false, state.isTextToSpeechAvailable)
        assertEquals(true, state.overheatWarningEnabled)
    }

    @Test
    fun `文言とTTS利用可否は閾値や有効状態とは独立して保持する`() {
        val state =
            Gt7Ps5ReadoutTyreTemperatureDetailUiState(
                readoutText = "注意{celsius}度",
                isTextToSpeechAvailable = true,
                overheatWarningEnabled = false,
                highThresholdCelsius = 105,
            )
        assertEquals("注意{celsius}度", state.readoutText)
        assertEquals(true, state.isTextToSpeechAvailable)
        assertEquals(false, state.overheatWarningEnabled)
        assertEquals(105, state.highThresholdCelsius)
    }
}
