package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureHighThresholdUseCase

internal class LmuWindowsReadoutTyreTemperatureDetailViewModel(
    observeHighThreshold: ObserveLmuWindowsTyreTemperatureHighThresholdUseCase,
    observeRaceFlags: ObserveLmuWindowsRaceFlagsUseCase,
    private val saveHighThreshold: SaveLmuWindowsTyreTemperatureHighThresholdUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {

    val uiState: StateFlow<LmuWindowsReadoutTyreTemperatureDetailUiState> =
        combine(observeHighThreshold(), observeRaceFlags()) { highThreshold, raceFlags ->
            LmuWindowsReadoutTyreTemperatureDetailUiState(
                highThresholdCelsius = highThreshold,
                gamePhase = raceFlags.gamePhase,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsReadoutTyreTemperatureDetailUiState(),
        )

    fun onHighThresholdChanged(celsius: Int) {
        viewModelScope.launch { saveHighThreshold(celsius) }
    }

    fun onHighThresholdReset() {
        viewModelScope.launch { saveHighThreshold(DEFAULT_HIGH_THRESHOLD_CELSIUS) }
    }

    fun onPreviewClicked() {
        playSpeechEvent(SpeechEvent.TyreOverheat)
    }

    companion object {
        const val DEFAULT_HIGH_THRESHOLD_CELSIUS = 90
    }
}
