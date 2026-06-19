package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.ObserveFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveFlagEnabledStateUseCase

internal class LmuWindowsReadoutFlagDetailViewModel(
    observeFlagEnabledStates: ObserveFlagEnabledStatesUseCase,
    private val saveFlagEnabledState: SaveFlagEnabledStateUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {

    val uiState: StateFlow<LmuWindowsReadoutFlagDetailUiState> = observeFlagEnabledStates()
        .map { storedStates ->
            val enabledStates = FlagReadoutItem.entries.associate { item ->
                item.key to (storedStates[item.key] ?: true)
            }
            LmuWindowsReadoutFlagDetailUiState(enabledStates = enabledStates)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LmuWindowsReadoutFlagDetailUiState())

    fun onFlagEnabledChanged(item: FlagReadoutItem, enabled: Boolean) {
        viewModelScope.launch { saveFlagEnabledState(item.key, enabled) }
    }

    fun onPreviewClicked(item: FlagReadoutItem) {
        playSpeechEvent(item.previewEvent)
    }
}
