package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase

internal class LmuWindowsReadoutTyreTemperatureDetailViewModel(
    observeHighThreshold: ObserveLmuWindowsTyreTemperatureHighThresholdUseCase,
    observeEnabledStates: ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase,
    observeLowWarningPhases: ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase,
    private val saveHighThreshold: SaveLmuWindowsTyreTemperatureHighThresholdUseCase,
    private val saveEnabledState: SaveLmuWindowsTyreTemperatureEnabledStateUseCase,
    private val saveLowWarningPhases: SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {

    val uiState: StateFlow<LmuWindowsReadoutTyreTemperatureDetailUiState> = combine(
        observeHighThreshold(),
        observeEnabledStates(),
        observeLowWarningPhases(),
    ) { highThresholdCelsius, states, lowWarningPhases ->
        LmuWindowsReadoutTyreTemperatureDetailUiState(
            highThresholdCelsius = highThresholdCelsius,
            overheatWarningEnabled = states.getValue(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning),
            lowWarningEnabled = states.getValue(ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning),
            lowWarningPhases = lowWarningPhases,
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

    fun onOverheatWarningEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, enabled)
        }
    }

    fun onPreviewClicked() {
        playSpeechEvent(SpeechEvent.TyreOverheat)
    }

    fun onLowWarningEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning, enabled)
        }
    }

    fun onLowWarningPhaseToggled(phase: SessionPhase) {
        val currentPhases = uiState.value.lowWarningPhases
        val updatedPhases = if (phase in currentPhases) currentPhases - phase else currentPhases + phase
        viewModelScope.launch { saveLowWarningPhases(updatedPhases) }
    }

    companion object {
        const val DEFAULT_HIGH_THRESHOLD_CELSIUS = 90
    }
}
