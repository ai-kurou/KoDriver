package kurou.kodriver.feature.acewindowsreadout.remainingfuellapsdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.readoutEnabled
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelLapsThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRemainingFuelLapsThresholdUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase

internal class AceWindowsReadoutRemainingFuelLapsDetailViewModel(
    observeAceWindowsRemainingFuelLapsThreshold: ObserveAceWindowsRemainingFuelLapsThresholdUseCase,
    private val saveAceWindowsRemainingFuelLapsThreshold: SaveAceWindowsRemainingFuelLapsThresholdUseCase,
    observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    private val saveReadoutEnabledState: SaveReadoutEnabledStateUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<AceWindowsReadoutRemainingFuelLapsDetailUiState> =
        combine(
            observeAceWindowsRemainingFuelLapsThreshold(),
            observeReadoutEnabledStates(Simulator.AceWindows.id),
        ) { remainingFuelLaps, enabledStates ->
            AceWindowsReadoutRemainingFuelLapsDetailUiState(
                remainingFuelLaps = remainingFuelLaps,
                enabled = enabledStates.readoutEnabled(ReadoutItemKey.AceWindows.RemainingFuelLaps.DetailEnabled),
            )
        }.stateIn(
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

    fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            saveReadoutEnabledState(
                Simulator.AceWindows.id,
                ReadoutItemKey.AceWindows.RemainingFuelLaps.DetailEnabled,
                enabled,
            )
        }
    }

    fun onPreviewClicked() {
        playSpeechEvent(SpeechEvent.AceWindowsRemainingFuelLapsWarning(uiState.value.remainingFuelLaps))
        playSpeechEvent(SpeechEvent.AceWindowsRemainingFuelLapsWarning(0), queue = true)
    }
}
