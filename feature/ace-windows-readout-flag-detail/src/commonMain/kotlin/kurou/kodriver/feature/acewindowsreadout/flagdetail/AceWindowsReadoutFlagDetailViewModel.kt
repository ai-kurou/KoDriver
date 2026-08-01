package kurou.kodriver.feature.acewindowsreadout.flagdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsFlagEnabledStateUseCase

internal class AceWindowsReadoutFlagDetailViewModel(
    observeFlagEnabledStates: ObserveAceWindowsFlagEnabledStatesUseCase,
    private val saveFlagEnabledState: SaveAceWindowsFlagEnabledStateUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<AceWindowsReadoutFlagDetailUiState> =
        observeFlagEnabledStates()
            .map { enabledStates -> AceWindowsReadoutFlagDetailUiState(enabledStates = enabledStates) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AceWindowsReadoutFlagDetailUiState())

    fun onFlagEnabledChanged(item: FlagReadoutItem, enabled: Boolean) {
        viewModelScope.launch { saveFlagEnabledState(item.key, enabled) }
    }

    fun onPreviewClicked(item: FlagReadoutItem) {
        playSpeechEvent(item.previewEvent)
    }
}
