package kurou.kodriver.feature.lmuwindowsreadout.mybestlapdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.usecase.ObserveLmuWindowsMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsMyBestLapVoiceTypeUseCase

internal class LmuWindowsReadoutMyBestLapDetailViewModel(
    observeMyBestLapVoiceType: ObserveLmuWindowsMyBestLapVoiceTypeUseCase,
    private val saveMyBestLapVoiceType: SaveLmuWindowsMyBestLapVoiceTypeUseCase,
) : ViewModel() {

    val uiState: StateFlow<LmuWindowsReadoutMyBestLapDetailUiState> =
        observeMyBestLapVoiceType()
            .map { LmuWindowsReadoutMyBestLapDetailUiState(voiceType = it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                LmuWindowsReadoutMyBestLapDetailUiState(),
            )

    fun onVoiceTypeChanged(type: MyBestLapVoiceType) {
        viewModelScope.launch {
            saveMyBestLapVoiceType(type)
        }
    }
}
