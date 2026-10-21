package kurou.kodriver.feature.otherandroidaudiousagedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.AndroidAudioUsage
import kurou.kodriver.domain.usecase.ObserveAndroidAudioUsageUseCase
import kurou.kodriver.domain.usecase.SaveAndroidAudioUsageUseCase

internal class OtherAndroidAudioUsageDetailViewModel(
    observeAndroidAudioUsage: ObserveAndroidAudioUsageUseCase,
    private val saveAndroidAudioUsage: SaveAndroidAudioUsageUseCase,
) : ViewModel() {
    val uiState: StateFlow<OtherAndroidAudioUsageDetailUiState> =
        observeAndroidAudioUsage().map { OtherAndroidAudioUsageDetailUiState(usage = it) }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            OtherAndroidAudioUsageDetailUiState(),
        )

    fun onUsageSelected(usage: AndroidAudioUsage) {
        viewModelScope.launch { saveAndroidAudioUsage(usage) }
    }
}
