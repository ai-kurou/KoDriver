package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsFlagEnabledStateUseCase

internal class LmuWindowsReadoutFlagDetailViewModel(
    observeFlagEnabledStates: ObserveLmuWindowsFlagEnabledStatesUseCase,
    private val saveFlagEnabledState: SaveLmuWindowsFlagEnabledStateUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {

    val uiState: StateFlow<LmuWindowsReadoutFlagDetailUiState> = observeFlagEnabledStates()
        .map { enabledStates -> LmuWindowsReadoutFlagDetailUiState(enabledStates = enabledStates) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LmuWindowsReadoutFlagDetailUiState())

    fun onFlagEnabledChanged(item: FlagReadoutItem, enabled: Boolean) {
        viewModelScope.launch { saveFlagEnabledState(item.key, enabled) }
    }

    fun onPreviewClicked(item: FlagReadoutItem) {
        playSpeechEvent(item.previewEvent)
    }
}
