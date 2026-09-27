package kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelLapsThresholdUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRemainingFuelLapsThresholdUseCase

internal class AceWindowsReadoutRemainingFuelLapsDetailViewModel(
    observeAceWindowsRemainingFuelLapsThreshold: ObserveAceWindowsRemainingFuelLapsThresholdUseCase,
    private val saveAceWindowsRemainingFuelLapsThreshold: SaveAceWindowsRemainingFuelLapsThresholdUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<AceWindowsReadoutRemainingFuelLapsDetailUiState> =
        observeAceWindowsRemainingFuelLapsThreshold()
            .map { AceWindowsReadoutRemainingFuelLapsDetailUiState(remainingFuelLaps = it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                AceWindowsReadoutRemainingFuelLapsDetailUiState(),
            )

    fun onRemainingFuelLapsChanged(laps: Int) {
        viewModelScope.launch {
            saveAceWindowsRemainingFuelLapsThreshold(laps)
        }
    }

    fun onResetRemainingFuelLaps() {
        onRemainingFuelLapsChanged(ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT)
    }

    fun onPreviewClicked() {
        playSpeechEvent(SpeechEvent.AceWindowsRemainingFuelLapsWarning(uiState.value.remainingFuelLaps))
        playSpeechEvent(SpeechEvent.AceWindowsRemainingFuelLapsWarning(0), queue = true)
    }
}
