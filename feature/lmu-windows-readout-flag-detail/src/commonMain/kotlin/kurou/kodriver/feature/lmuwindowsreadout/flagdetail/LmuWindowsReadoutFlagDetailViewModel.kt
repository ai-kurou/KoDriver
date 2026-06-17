package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.usecase.ObserveFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveFlagEnabledStateUseCase

private val flagKeys = listOf(
    ReadoutItemKey.BLUE_FLAG,
    ReadoutItemKey.SECTOR_YELLOW_FLAG,
    ReadoutItemKey.FULL_COURSE_YELLOW,
    ReadoutItemKey.RED_FLAG,
)

private val flagKeyToSpeechEvent = mapOf(
    ReadoutItemKey.BLUE_FLAG to SpeechEvent.BlueFlag,
    ReadoutItemKey.SECTOR_YELLOW_FLAG to SpeechEvent.YellowFlag,
    ReadoutItemKey.FULL_COURSE_YELLOW to SpeechEvent.FullCourseYellow,
    ReadoutItemKey.RED_FLAG to SpeechEvent.SessionStop,
)

internal class LmuWindowsReadoutFlagDetailViewModel(
    observeFlagEnabledStates: ObserveFlagEnabledStatesUseCase,
    private val saveFlagEnabledState: SaveFlagEnabledStateUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {

    val uiState: StateFlow<LmuWindowsReadoutFlagDetailUiState> = observeFlagEnabledStates()
        .map { storedStates ->
            val enabledStates = flagKeys.associateWith { storedStates[it] ?: true }
            LmuWindowsReadoutFlagDetailUiState(enabledStates = enabledStates)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LmuWindowsReadoutFlagDetailUiState())

    fun onFlagEnabledChanged(key: String, enabled: Boolean) {
        viewModelScope.launch { saveFlagEnabledState(key, enabled) }
    }

    fun onPreviewClicked(key: String) {
        val event = flagKeyToSpeechEvent[key] ?: return
        playSpeechEvent(event)
    }
}
