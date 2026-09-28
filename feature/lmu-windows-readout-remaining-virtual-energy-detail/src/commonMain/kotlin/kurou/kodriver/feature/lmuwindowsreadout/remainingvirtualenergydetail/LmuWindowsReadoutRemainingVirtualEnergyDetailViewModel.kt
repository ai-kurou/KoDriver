package kurou.kodriver.feature.lmuwindowsreadout.remainingvirtualenergydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.readoutEnabled
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase

internal class LmuWindowsReadoutRemainingVirtualEnergyDetailViewModel(
    observeThresholdPercentage: ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase,
    private val saveThresholdPercentage: SaveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase,
    observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    private val saveReadoutEnabledState: SaveReadoutEnabledStateUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<LmuWindowsReadoutRemainingVirtualEnergyDetailUiState> =
        combine(
            observeThresholdPercentage(),
            observeReadoutEnabledStates(Simulator.LmuWindows.id),
        ) { thresholdPercentage, enabledStates ->
            LmuWindowsReadoutRemainingVirtualEnergyDetailUiState(
                thresholdPercentage = thresholdPercentage,
                enabled = enabledStates.readoutEnabled(ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root),
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsReadoutRemainingVirtualEnergyDetailUiState(),
        )

    fun onWarningChipClicked() {
        playSpeechEvent(SpeechEvent.RemainingVirtualEnergyWarning)
    }

    fun onThresholdChanged(percentage: Int) {
        viewModelScope.launch { saveThresholdPercentage(percentage) }
    }

    fun onThresholdReset() {
        viewModelScope.launch {
            saveThresholdPercentage(LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_THRESHOLD_PERCENTAGE_DEFAULT)
        }
    }

    fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            saveReadoutEnabledState(
                Simulator.LmuWindows.id,
                ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root,
                enabled,
            )
        }
    }
}
