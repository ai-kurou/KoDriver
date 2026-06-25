package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.usecase.ObserveLateralThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveLongitudinalThresholdUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLateralThresholdUseCase
import kurou.kodriver.domain.usecase.SaveLongitudinalThresholdUseCase
import kurou.kodriver.domain.usecase.VehicleApproachPreferencesUseCases

internal class LmuWindowsReadoutVehicleApproachDetailViewModel(
    observeLateralThreshold: ObserveLateralThresholdUseCase,
    observeLongitudinalThreshold: ObserveLongitudinalThresholdUseCase,
    private val vehicleApproachPreferences: VehicleApproachPreferencesUseCases,
    private val saveLateralThreshold: SaveLateralThresholdUseCase,
    private val saveLongitudinalThreshold: SaveLongitudinalThresholdUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {

    val uiState: StateFlow<LmuWindowsReadoutVehicleApproachDetailUiState> = combine(
        observeLateralThreshold(),
        observeLongitudinalThreshold(),
        vehicleApproachPreferences.observeSkipFirstLap(),
        vehicleApproachPreferences.observeStartReadoutEnabled(),
        vehicleApproachPreferences.observeStartReadoutType(),
    ) { lateral, longitudinal, skipFirstLap, startReadoutEnabled, startReadoutType ->
        LmuWindowsReadoutVehicleApproachDetailUiState(
            lateralThresholdMeters = lateral,
            longitudinalThresholdMeters = longitudinal,
            skipFirstLap = skipFirstLap,
            startReadoutEnabled = startReadoutEnabled,
            startReadoutType = startReadoutType,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LmuWindowsReadoutVehicleApproachDetailUiState())

    fun onLateralThresholdChanged(meters: Double) {
        viewModelScope.launch { saveLateralThreshold(meters) }
    }

    fun onLongitudinalThresholdChanged(meters: Double) {
        viewModelScope.launch { saveLongitudinalThreshold(meters) }
    }

    fun onResetLongitudinalThreshold() {
        viewModelScope.launch { saveLongitudinalThreshold(DEFAULT_LONGITUDINAL_THRESHOLD_METERS) }
    }

    fun onResetLateralThreshold() {
        viewModelScope.launch { saveLateralThreshold(DEFAULT_LATERAL_THRESHOLD_METERS) }
    }

    fun onSkipFirstLapChanged(skip: Boolean) {
        viewModelScope.launch { vehicleApproachPreferences.saveSkipFirstLap(skip) }
    }

    fun onStartReadoutEnabledChanged(enabled: Boolean) {
        viewModelScope.launch { vehicleApproachPreferences.saveStartReadoutEnabled(enabled) }
    }

    fun onStartReadoutTypeChanged(type: VehicleApproachStartReadoutType) {
        viewModelScope.launch { vehicleApproachPreferences.saveStartReadoutType(type) }
        playStartReadoutPreview(type)
    }

    fun onStartReadoutPreviewClicked() {
        playStartReadoutPreview(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
    }

    private fun playStartReadoutPreview(type: VehicleApproachStartReadoutType) {
        val events = when (type) {
            VehicleApproachStartReadoutType.CAR_LEFT_RIGHT -> SpeechEvent.CarLeft to SpeechEvent.CarRight
            VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH -> SpeechEvent.LeftApproach to SpeechEvent.RightApproach
        }
        playSpeechEvent(events.first)
        playSpeechEvent(events.second, queue = true)
    }

    companion object {
        const val DEFAULT_LONGITUDINAL_THRESHOLD_METERS = 5.0
        const val DEFAULT_LATERAL_THRESHOLD_METERS = 5.0
    }
}
