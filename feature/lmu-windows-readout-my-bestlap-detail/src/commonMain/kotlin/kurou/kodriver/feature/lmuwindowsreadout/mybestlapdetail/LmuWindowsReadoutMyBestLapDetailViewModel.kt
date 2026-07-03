package kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.MyBestLapVoiceType

internal class LmuWindowsReadoutMyBestLapDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LmuWindowsReadoutMyBestLapDetailUiState())
    val uiState: StateFlow<LmuWindowsReadoutMyBestLapDetailUiState> = _uiState

    fun onVoiceTypeChanged(type: MyBestLapVoiceType) {
        _uiState.update { it.copy(voiceType = type) }
    }
}
