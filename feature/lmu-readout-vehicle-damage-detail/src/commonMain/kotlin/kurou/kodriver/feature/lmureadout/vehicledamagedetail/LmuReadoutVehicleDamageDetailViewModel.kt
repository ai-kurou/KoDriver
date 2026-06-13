package kurou.kodriver.feature.lmureadout.vehicledamagedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.usecase.ObserveVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.usecase.SaveVehicleDamageEnabledStateUseCase

internal class LmuReadoutVehicleDamageDetailViewModel(
    observeEnabledStates: ObserveVehicleDamageEnabledStatesUseCase,
    private val saveEnabledState: SaveVehicleDamageEnabledStateUseCase,
) : ViewModel() {

    val uiState: StateFlow<LmuReadoutVehicleDamageDetailUiState> = observeEnabledStates()
        .map { states ->
            LmuReadoutVehicleDamageDetailUiState(
                overheatEnabled = states[ReadoutItemKey.OVERHEAT] ?: true,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LmuReadoutVehicleDamageDetailUiState())

    fun onOverheatEnabledChanged(enabled: Boolean) {
        viewModelScope.launch { saveEnabledState(ReadoutItemKey.OVERHEAT, enabled) }
    }
}
