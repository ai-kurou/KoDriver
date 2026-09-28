package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.readoutEnabled
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase

internal class Gt7Ps5ReadoutRemainingFuelLapsDetailViewModel(
    observeGt7Ps5RemainingFuelLaps: ObserveGt7Ps5RemainingFuelLapsUseCase,
    private val saveGt7Ps5RemainingFuelLaps: SaveGt7Ps5RemainingFuelLapsUseCase,
    observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    private val saveReadoutEnabledState: SaveReadoutEnabledStateUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<Gt7Ps5ReadoutRemainingFuelLapsDetailUiState> =
        combine(
            observeGt7Ps5RemainingFuelLaps(),
            observeReadoutEnabledStates(Simulator.Gt7Ps5.id),
        ) { remainingFuelLaps, enabledStates ->
            Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(
                remainingFuelLaps = remainingFuelLaps,
                enabled = enabledStates.readoutEnabled(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.DetailEnabled),
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(),
        )

    fun onRemainingFuelLapsChanged(laps: Int) {
        viewModelScope.launch {
            saveGt7Ps5RemainingFuelLaps(laps)
        }
    }

    fun onResetRemainingFuelLaps() {
        onRemainingFuelLapsChanged(GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT)
    }

    fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            saveReadoutEnabledState(Simulator.Gt7Ps5.id, ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.DetailEnabled, enabled)
        }
    }

    fun onPreviewClicked() {
        playSpeechEvent(SpeechEvent.RemainingFuelLapsWarning(uiState.value.remainingFuelLaps))
        playSpeechEvent(SpeechEvent.RemainingFuelLapsWarning(0), queue = true)
    }
}
