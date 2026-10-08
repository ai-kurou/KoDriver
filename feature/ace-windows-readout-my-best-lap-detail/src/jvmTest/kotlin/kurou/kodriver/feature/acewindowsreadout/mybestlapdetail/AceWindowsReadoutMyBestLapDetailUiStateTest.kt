package kurou.kodriver.feature.acewindowsreadout.mybestlapdetail

import kurou.kodriver.domain.model.ACE_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsReadoutMyBestLapDetailUiStateTest {
    @Test
    fun `初期状態はドメインの既定値を使用しTTSは利用不可`() {
        val state = AceWindowsReadoutMyBestLapDetailUiState()
        assertEquals(ACE_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT, state.readoutText)
        assertEquals(false, state.isTextToSpeechAvailable)
        assertEquals(true, state.enabled)
    }

    @Test
    fun `文言とTTS利用可否は有効状態とは独立して保持する`() {
        val state =
            AceWindowsReadoutMyBestLapDetailUiState(
                readoutText = "注意{laptime}",
                isTextToSpeechAvailable = true,
                enabled = false,
            )
        assertEquals("注意{laptime}", state.readoutText)
        assertEquals(true, state.isTextToSpeechAvailable)
        assertEquals(false, state.enabled)
    }
}
