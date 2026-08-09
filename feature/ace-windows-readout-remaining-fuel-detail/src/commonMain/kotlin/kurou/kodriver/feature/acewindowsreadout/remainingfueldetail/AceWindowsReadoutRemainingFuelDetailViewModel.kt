package kurou.kodriver.feature.acewindowsreadout.remainingfueldetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRemainingFuelThresholdPercentageUseCase

internal class AceWindowsReadoutRemainingFuelDetailViewModel(
    observeThresholdPercentage: ObserveAceWindowsRemainingFuelThresholdPercentageUseCase,
    private val saveThresholdPercentage: SaveAceWindowsRemainingFuelThresholdPercentageUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<AceWindowsReadoutRemainingFuelDetailUiState> =
        observeThresholdPercentage()
            .map { AceWindowsReadoutRemainingFuelDetailUiState(thresholdPercentage = it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                AceWindowsReadoutRemainingFuelDetailUiState(),
            )

    fun onThresholdChanged(percentage: Int) {
        viewModelScope.launch { saveThresholdPercentage(percentage) }
    }

    fun onThresholdReset() {
        viewModelScope.launch { saveThresholdPercentage(ACE_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT) }
    }

    fun onPreviewClicked() {
        playSpeechEvent(SpeechEvent.AceWindowsRemainingFuelWarning)
    }
}
