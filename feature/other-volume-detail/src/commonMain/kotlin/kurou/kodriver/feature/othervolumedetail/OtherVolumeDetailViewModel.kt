package kurou.kodriver.feature.othervolumedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.GetDeviceVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SetDeviceVolumeUseCase

internal class OtherVolumeDetailViewModel(
    observeSoundVolume: ObserveSoundVolumeUseCase,
    private val saveSoundVolume: SaveSoundVolumeUseCase,
    private val getDeviceVolume: GetDeviceVolumeUseCase,
    private val setDeviceVolume: SetDeviceVolumeUseCase,
) : ViewModel() {
    private val deviceVolumeRefreshTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<OtherVolumeDetailUiState> =
        combine(
            observeSoundVolume(),
            deviceVolumeRefreshTrigger.flatMapLatest { flow { emit(getDeviceVolume()) } },
        ) { volume, deviceVolume ->
            OtherVolumeDetailUiState(volume = volume, deviceVolume = deviceVolume)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OtherVolumeDetailUiState())

    fun onVolumeChanged(volume: Int) {
        viewModelScope.launch { saveSoundVolume(volume) }
    }

    fun onDeviceVolumeChanged(volume: Int) {
        viewModelScope.launch {
            setDeviceVolume(volume)
            deviceVolumeRefreshTrigger.update { it + 1 }
        }
    }
}
