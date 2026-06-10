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
import kurou.kodriver.domain.usecase.ObserveSkipFirstLapUseCase
import kurou.kodriver.domain.usecase.SaveLateralThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLongitudinalThresholdUseCase
import kurou.kodriver.domain.usecase.SaveSkipFirstLapUseCase

internal class VehicleApproachViewModel(
    observeLateralThreshold: ObserveLateralThresholdUseCase,
    observeLongitudinalThreshold: ObserveLongitudinalThresholdUseCase,
    observeSkipFirstLap: ObserveSkipFirstLapUseCase,
    private val saveLateralThreshold: SaveLateralThresholdUseCase,
    private val saveLongitudinalThreshold: SaveLongitudinalThresholdUseCase,
    private val saveSkipFirstLap: SaveSkipFirstLapUseCase,
) : ViewModel() {

    val uiState: StateFlow<VehicleApproachUiState> = combine(
        observeLateralThreshold(),
        observeLongitudinalThreshold(),
        observeSkipFirstLap(),
    ) { lateral, longitudinal, skipFirstLap ->
        VehicleApproachUiState(
            lateralThresholdMeters = lateral,
            longitudinalThresholdMeters = longitudinal,
            skipFirstLap = skipFirstLap,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), VehicleApproachUiState())

    fun onLateralThresholdChanged(meters: Double) {
        viewModelScope.launch { saveLateralThreshold(meters) }
    }

    fun onLongitudinalThresholdChanged(meters: Double) {
        viewModelScope.launch { saveLongitudinalThreshold(meters) }
    }

    fun onSkipFirstLapChanged(skip: Boolean) {
        viewModelScope.launch { saveSkipFirstLap(skip) }
    }
}
