package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleDamageEnabledStateUseCase

internal class LmuWindowsReadoutVehicleDamageDetailViewModel(
    observeEnabledStates: ObserveLmuWindowsVehicleDamageEnabledStatesUseCase,
    private val saveEnabledState: SaveLmuWindowsVehicleDamageEnabledStateUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {

    val uiState: StateFlow<LmuWindowsReadoutVehicleDamageDetailUiState> = observeEnabledStates()
        .map { states ->
            LmuWindowsReadoutVehicleDamageDetailUiState(
                overheatEnabled = states.getValue(ReadoutItemKey.Overheat),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LmuWindowsReadoutVehicleDamageDetailUiState())

    fun onOverheatEnabledChanged(enabled: Boolean) {
        viewModelScope.launch { saveEnabledState(ReadoutItemKey.Overheat, enabled) }
    }

    fun onPreviewClicked() {
        playSpeechEvent(SpeechEvent.Overheating)
    }
}
