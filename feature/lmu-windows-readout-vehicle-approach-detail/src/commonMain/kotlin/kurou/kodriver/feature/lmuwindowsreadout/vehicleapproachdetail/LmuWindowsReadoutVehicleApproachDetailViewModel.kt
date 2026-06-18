package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

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
import kurou.kodriver.domain.usecase.VehicleApproachPreferencesUseCases

internal class LmuWindowsReadoutVehicleApproachDetailViewModel(
    observeLateralThreshold: ObserveLateralThresholdUseCase,
    observeLongitudinalThreshold: ObserveLongitudinalThresholdUseCase,
    private val vehicleApproachPreferences: VehicleApproachPreferencesUseCases,
    private val saveLateralThreshold: SaveLateralThresholdUseCase,
    private val saveLongitudinalThreshold: SaveLongitudinalThresholdUseCase,
) : ViewModel() {

    val uiState: StateFlow<LmuWindowsReadoutVehicleApproachDetailUiState> = combine(
        observeLateralThreshold(),
        observeLongitudinalThreshold(),
        vehicleApproachPreferences.observeSkipFirstLap(),
        vehicleApproachPreferences.observeStartReadoutEnabled(),
    ) { lateral, longitudinal, skipFirstLap, startReadoutEnabled ->
        LmuWindowsReadoutVehicleApproachDetailUiState(
            lateralThresholdMeters = lateral,
            longitudinalThresholdMeters = longitudinal,
            skipFirstLap = skipFirstLap,
            startReadoutEnabled = startReadoutEnabled,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LmuWindowsReadoutVehicleApproachDetailUiState())

    fun onLateralThresholdChanged(meters: Double) {
        viewModelScope.launch { saveLateralThreshold(meters) }
    }

    fun onLongitudinalThresholdChanged(meters: Double) {
        viewModelScope.launch { saveLongitudinalThreshold(meters) }
    }

    fun onSkipFirstLapChanged(skip: Boolean) {
        viewModelScope.launch { vehicleApproachPreferences.saveSkipFirstLap(skip) }
    }

    fun onStartReadoutEnabledChanged(enabled: Boolean) {
        viewModelScope.launch { vehicleApproachPreferences.saveStartReadoutEnabled(enabled) }
    }
}
