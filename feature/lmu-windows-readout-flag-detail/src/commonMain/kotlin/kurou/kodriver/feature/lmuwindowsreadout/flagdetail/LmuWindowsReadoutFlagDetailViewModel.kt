package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.RedFlagVoiceType
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagVoiceTypeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsFlagEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsRedFlagVoiceTypeUseCase

internal class LmuWindowsReadoutFlagDetailViewModel(
    observeFlagEnabledStates: ObserveLmuWindowsFlagEnabledStatesUseCase,
    observeRedFlagVoiceType: ObserveLmuWindowsRedFlagVoiceTypeUseCase,
    private val saveFlagEnabledState: SaveLmuWindowsFlagEnabledStateUseCase,
    private val saveRedFlagVoiceType: SaveLmuWindowsRedFlagVoiceTypeUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<LmuWindowsReadoutFlagDetailUiState> =
        combine(observeFlagEnabledStates(), observeRedFlagVoiceType()) { enabledStates, redFlagVoiceType ->
            LmuWindowsReadoutFlagDetailUiState(enabledStates = enabledStates, redFlagVoiceType = redFlagVoiceType)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LmuWindowsReadoutFlagDetailUiState())

    fun onFlagEnabledChanged(
        item: FlagReadoutItem,
        enabled: Boolean,
    ) {
        viewModelScope.launch { saveFlagEnabledState(item.key, enabled) }
    }

    fun onRedFlagEnabledChanged(enabled: Boolean) {
        viewModelScope.launch { saveFlagEnabledState(ReadoutItemKey.LmuWindows.Flag.RedFlag, enabled) }
    }

    fun onPreviewClicked(item: FlagReadoutItem) {
        playSpeechEvent(item.previewEvent)
    }

    fun onRedFlagVoiceTypeChanged(type: RedFlagVoiceType) {
        viewModelScope.launch { saveRedFlagVoiceType(type) }
    }

    fun onRedFlagPreviewClicked(type: RedFlagVoiceType) {
        playSpeechEvent(
            when (type) {
                RedFlagVoiceType.RED_FLAG -> SpeechEvent.RedFlag
                RedFlagVoiceType.SESSION_STOP -> SpeechEvent.SessionStop
            },
        )
    }
}
