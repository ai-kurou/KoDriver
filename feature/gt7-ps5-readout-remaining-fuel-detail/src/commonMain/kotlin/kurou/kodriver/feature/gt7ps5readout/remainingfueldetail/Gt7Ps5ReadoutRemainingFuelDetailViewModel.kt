package kurou.kodriver.feature.gt7ps5readout.remainingfueldetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.readoutEnabled
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase

internal class Gt7Ps5ReadoutRemainingFuelDetailViewModel(
    observeThresholdPercentage: ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase,
    private val saveThresholdPercentage: SaveGt7Ps5RemainingFuelThresholdPercentageUseCase,
    observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    private val saveReadoutEnabledState: SaveReadoutEnabledStateUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<Gt7Ps5ReadoutRemainingFuelDetailUiState> =
        combine(
            observeThresholdPercentage(),
            observeReadoutEnabledStates(Simulator.Gt7Ps5.id),
        ) { thresholdPercentage, enabledStates ->
            Gt7Ps5ReadoutRemainingFuelDetailUiState(
                thresholdPercentage = thresholdPercentage,
                enabled = enabledStates.readoutEnabled(ReadoutItemKey.Gt7Ps5.RemainingFuel.DetailEnabled),
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            Gt7Ps5ReadoutRemainingFuelDetailUiState(),
        )

    fun onThresholdChanged(percentage: Int) {
        viewModelScope.launch { saveThresholdPercentage(percentage) }
    }

    fun onThresholdReset() {
        onThresholdChanged(GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT)
    }

    fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            saveReadoutEnabledState(Simulator.Gt7Ps5.id, ReadoutItemKey.Gt7Ps5.RemainingFuel.DetailEnabled, enabled)
        }
    }

    fun onPreviewClicked() {
        playSpeechEvent(SpeechEvent.Gt7Ps5RemainingFuelWarning)
    }
}
