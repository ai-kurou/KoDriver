package kurou.kodriver.feature.readout.vehicleapproach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.ObserveLateralThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLongitudinalThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLateralThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLongitudinalThresholdUseCase

internal class VehicleApproachViewModel(
    observeLateralThreshold: ObserveLateralThresholdUseCase,
    observeLongitudinalThreshold: ObserveLongitudinalThresholdUseCase,
    private val saveLateralThreshold: SaveLateralThresholdUseCase,
    private val saveLongitudinalThreshold: SaveLongitudinalThresholdUseCase,
) : ViewModel() {

    val uiState: StateFlow<VehicleApproachUiState> = combine(
        observeLateralThreshold(),
        observeLongitudinalThreshold(),
    ) { lateral, longitudinal ->
        VehicleApproachUiState(
            lateralThresholdMeters = lateral,
            longitudinalThresholdMeters = longitudinal,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), VehicleApproachUiState())

    fun onLateralThresholdChanged(meters: Double) {
        viewModelScope.launch { saveLateralThreshold(meters) }
    }

    fun onLongitudinalThresholdChanged(meters: Double) {
        viewModelScope.launch { saveLongitudinalThreshold(meters) }
    }
}
