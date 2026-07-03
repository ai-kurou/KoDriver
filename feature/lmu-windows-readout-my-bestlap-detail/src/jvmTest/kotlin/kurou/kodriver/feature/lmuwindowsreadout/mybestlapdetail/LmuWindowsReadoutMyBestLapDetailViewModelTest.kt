package kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail

import kurou.kodriver.domain.model.MyBestLapVoiceType
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutMyBestLapDetailViewModelTest {

    @Test
    fun `初期状態は通常音声`() {
        val viewModel = LmuWindowsReadoutMyBestLapDetailViewModel()

        assertEquals(MyBestLapVoiceType.FORMAL, viewModel.uiState.value.voiceType)
    }

    @Test
    fun `音声タイプを変更するとUI状態へ反映される`() {
        val viewModel = LmuWindowsReadoutMyBestLapDetailViewModel()

        viewModel.onVoiceTypeChanged(MyBestLapVoiceType.CASUAL)

        assertEquals(MyBestLapVoiceType.CASUAL, viewModel.uiState.value.voiceType)
    }
}
