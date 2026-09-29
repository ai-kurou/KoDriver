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

internal data class RemainingVirtualEnergyUseCases(
    val observeThresholdPercentage: ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase,
    val saveThresholdPercentage: SaveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase,
    val observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    val saveReadoutEnabledState: SaveReadoutEnabledStateUseCase,
)

internal class LmuWindowsReadoutRemainingVirtualEnergyDetailViewModel(
    private val remainingVirtualEnergyUseCases: RemainingVirtualEnergyUseCases,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<LmuWindowsReadoutRemainingVirtualEnergyDetailUiState> =
        combine(
            remainingVirtualEnergyUseCases.observeThresholdPercentage(),
            remainingVirtualEnergyUseCases.observeReadoutEnabledStates(Simulator.LmuWindows.id),
        ) { thresholdPercentage, enabledStates ->
            LmuWindowsReadoutRemainingVirtualEnergyDetailUiState(
                thresholdPercentage = thresholdPercentage,
                enabled = enabledStates.readoutEnabled(ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.WarningReadout),
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
        viewModelScope.launch { remainingVirtualEnergyUseCases.saveThresholdPercentage(percentage) }
    }

    fun onThresholdReset() {
        viewModelScope.launch {
            remainingVirtualEnergyUseCases.saveThresholdPercentage(
                LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_THRESHOLD_PERCENTAGE_DEFAULT,
            )
        }
    }

    fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            remainingVirtualEnergyUseCases.saveReadoutEnabledState(
                Simulator.LmuWindows.id,
                ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.WarningReadout,
                enabled,
            )
        }
    }
}
