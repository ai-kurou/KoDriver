package kurou.kodriver.feature.acewindowsreadout.remainingfueldetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.readoutEnabled
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase

internal class AceWindowsReadoutRemainingFuelDetailViewModel(
    observeThresholdPercentage: ObserveAceWindowsRemainingFuelThresholdPercentageUseCase,
    private val saveThresholdPercentage: SaveAceWindowsRemainingFuelThresholdPercentageUseCase,
    observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    private val saveReadoutEnabledState: SaveReadoutEnabledStateUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<AceWindowsReadoutRemainingFuelDetailUiState> =
        combine(
            observeThresholdPercentage(),
            observeReadoutEnabledStates(Simulator.AceWindows.id),
        ) { thresholdPercentage, enabledStates ->
            AceWindowsReadoutRemainingFuelDetailUiState(
                thresholdPercentage = thresholdPercentage,
                enabled = enabledStates.readoutEnabled(ReadoutItemKey.AceWindows.RemainingFuel.DetailEnabled),
            )
        }.stateIn(
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

    fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            saveReadoutEnabledState(
                Simulator.AceWindows.id,
                ReadoutItemKey.AceWindows.RemainingFuel.DetailEnabled,
                enabled,
            )
        }
    }

    fun onPreviewClicked() {
        playSpeechEvent(SpeechEvent.AceWindowsRemainingFuelWarning)
    }
}
