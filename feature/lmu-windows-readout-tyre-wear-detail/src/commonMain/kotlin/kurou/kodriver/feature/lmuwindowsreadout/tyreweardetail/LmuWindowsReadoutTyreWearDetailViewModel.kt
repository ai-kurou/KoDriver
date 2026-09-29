package kurou.kodriver.feature.lmuwindowsreadout.tyreweardetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.readoutEnabled
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsTyreWearThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase

internal data class TyreWearUseCases(
    val observeThresholdPercentage: ObserveLmuWindowsTyreWearThresholdPercentageUseCase,
    val saveThresholdPercentage: SaveLmuWindowsTyreWearThresholdPercentageUseCase,
    val observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    val saveReadoutEnabledState: SaveReadoutEnabledStateUseCase,
)

internal class LmuWindowsReadoutTyreWearDetailViewModel(
    private val tyreWearUseCases: TyreWearUseCases,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    val uiState: StateFlow<LmuWindowsReadoutTyreWearDetailUiState> =
        combine(
            tyreWearUseCases.observeThresholdPercentage(),
            tyreWearUseCases.observeReadoutEnabledStates(Simulator.LmuWindows.id),
        ) { thresholdPercentage, enabledStates ->
            LmuWindowsReadoutTyreWearDetailUiState(
                thresholdPercentage = thresholdPercentage,
                enabled = enabledStates.readoutEnabled(ReadoutItemKey.LmuWindows.TyreWear.WarningReadout),
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsReadoutTyreWearDetailUiState(),
        )

    fun onWarningChipClicked() {
        playSpeechEvent(SpeechEvent.TyreWearWarning)
    }

    fun onThresholdChanged(percentage: Int) {
        viewModelScope.launch { tyreWearUseCases.saveThresholdPercentage(percentage) }
    }

    fun onThresholdReset() {
        viewModelScope.launch {
            tyreWearUseCases.saveThresholdPercentage(LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT)
        }
    }

    fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            tyreWearUseCases.saveReadoutEnabledState(
                Simulator.LmuWindows.id,
                ReadoutItemKey.LmuWindows.TyreWear.WarningReadout,
                enabled,
            )
        }
    }
}
