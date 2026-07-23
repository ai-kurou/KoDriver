package kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase

internal class LmuWindowsReadoutTyreWearDetailViewModel(
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LmuWindowsReadoutTyreWearDetailUiState())
    val uiState: StateFlow<LmuWindowsReadoutTyreWearDetailUiState> = _uiState.asStateFlow()

    fun onWarningChipClicked() {
        playSpeechEvent(SpeechEvent.TyreWearWarning)
    }

    fun onThresholdChanged(percentage: Int) {
        _uiState.update { it.copy(thresholdPercentage = percentage) }
    }

    companion object {
        const val DEFAULT_THRESHOLD_PERCENTAGE = 50
    }
}
