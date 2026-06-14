package kurou.kodriver.feature.othervolumedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveSoundVolumeUseCase

internal class OtherVolumeDetailViewModel(
    observeSoundVolume: ObserveSoundVolumeUseCase,
    private val saveSoundVolume: SaveSoundVolumeUseCase,
) : ViewModel() {

    val uiState: StateFlow<OtherVolumeDetailUiState> = observeSoundVolume()
        .map { OtherVolumeDetailUiState(volume = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OtherVolumeDetailUiState())

    fun onVolumeChanged(volume: Int) {
        viewModelScope.launch { saveSoundVolume(volume) }
    }
}
