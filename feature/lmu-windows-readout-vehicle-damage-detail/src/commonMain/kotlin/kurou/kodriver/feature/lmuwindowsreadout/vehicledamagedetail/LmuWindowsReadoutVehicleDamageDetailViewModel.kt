package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    private val partDetachedEnabled = MutableStateFlow(true)

    val uiState: StateFlow<LmuWindowsReadoutVehicleDamageDetailUiState> =
        combine(observeEnabledStates(), partDetachedEnabled) { states, partDetachedEnabled ->
            LmuWindowsReadoutVehicleDamageDetailUiState(
                overheatEnabled = states.getValue(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat),
                partDetachedEnabled = partDetachedEnabled,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsReadoutVehicleDamageDetailUiState(),
        )

    fun onOverheatEnabledChanged(enabled: Boolean) {
        viewModelScope.launch { saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, enabled) }
    }

    fun onPreviewClicked() {
        playSpeechEvent(SpeechEvent.Overheating)
    }

    fun onPartDetachedEnabledChanged(enabled: Boolean) {
        partDetachedEnabled.value = enabled
    }
}
