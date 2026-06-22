package kurou.kodriver.feature.gt7ps5readout.mybestlapdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.usecase.ObserveMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveMyBestLapVoiceTypeUseCase

internal class Gt7Ps5ReadoutMyBestLapDetailViewModel(
    observeMyBestLapVoiceType: ObserveMyBestLapVoiceTypeUseCase,
    private val saveMyBestLapVoiceType: SaveMyBestLapVoiceTypeUseCase,
) : ViewModel() {

    val uiState: StateFlow<Gt7Ps5ReadoutMyBestLapDetailUiState> =
        observeMyBestLapVoiceType()
            .map { Gt7Ps5ReadoutMyBestLapDetailUiState(voiceType = it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                Gt7Ps5ReadoutMyBestLapDetailUiState(),
            )

    fun onVoiceTypeChanged(type: MyBestLapVoiceType) {
        viewModelScope.launch {
            saveMyBestLapVoiceType(type)
        }
    }
}
