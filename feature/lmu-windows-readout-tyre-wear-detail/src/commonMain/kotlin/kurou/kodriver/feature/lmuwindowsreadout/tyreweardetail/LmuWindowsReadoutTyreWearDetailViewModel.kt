package kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_WEAR_DEFAULT_THRESHOLD_PERCENTAGE
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreWearThresholdPercentageUseCase

internal class LmuWindowsReadoutTyreWearDetailViewModel(
    observeThresholdPercentage: ObserveLmuWindowsTyreWearThresholdPercentageUseCase,
    private val saveThresholdPercentage: SaveLmuWindowsTyreWearThresholdPercentageUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<LmuWindowsReadoutTyreWearDetailUiState> =
        observeThresholdPercentage()
            .map { LmuWindowsReadoutTyreWearDetailUiState(thresholdPercentage = it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                LmuWindowsReadoutTyreWearDetailUiState(),
            )

    fun onWarningChipClicked() {
        playSpeechEvent(SpeechEvent.TyreWearWarning)
    }

    fun onThresholdChanged(percentage: Int) {
        viewModelScope.launch { saveThresholdPercentage(percentage) }
    }

    fun onThresholdReset() {
        viewModelScope.launch { saveThresholdPercentage(LMU_WINDOWS_TYRE_WEAR_DEFAULT_THRESHOLD_PERCENTAGE) }
    }
}
