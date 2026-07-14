package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.usecase.LmuWindowsVehicleApproachPreferencesUseCases
import kurou.kodriver.domain.usecase.LmuWindowsVehicleApproachThresholdsUseCases
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleApproachEnabledStateUseCase

internal class LmuWindowsReadoutVehicleApproachDetailViewModel(
    private val thresholds: LmuWindowsVehicleApproachThresholdsUseCases,
    private val vehicleApproachPreferences: LmuWindowsVehicleApproachPreferencesUseCases,
    private val observeEnabledStates: ObserveLmuWindowsVehicleApproachEnabledStatesUseCase,
    private val saveEnabledState: SaveLmuWindowsVehicleApproachEnabledStateUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {

    val uiState: StateFlow<LmuWindowsReadoutVehicleApproachDetailUiState> = combine(
        combine(
            thresholds.observeLateralThresholdMeters(),
            thresholds.observeLongitudinalThresholdMeters(),
            thresholds.observeSustainedApproachDurationSeconds(),
        ) { lateral, longitudinal, sustainedDuration -> Triple(lateral, longitudinal, sustainedDuration) },
        vehicleApproachPreferences.observeSkipFirstLap(),
        observeEnabledStates(),
        vehicleApproachPreferences.observeStartReadoutType(),
    ) { thresholdValues, skipFirstLap, enabledStates, startReadoutType ->
        val (lateral, longitudinal, sustainedDuration) = thresholdValues
        LmuWindowsReadoutVehicleApproachDetailUiState(
            lateralThresholdMeters = lateral,
            longitudinalThresholdMeters = longitudinal,
            sustainedApproachDurationSeconds = sustainedDuration,
            skipFirstLap = skipFirstLap,
            startReadoutEnabled = enabledStates.getValue(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout),
            startReadoutType = startReadoutType,
            sustainedReadoutEnabled = enabledStates.getValue(ReadoutItemKey.LmuWindows.VehicleApproach.Sustained),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LmuWindowsReadoutVehicleApproachDetailUiState())

    fun onLateralThresholdChanged(meters: Double) {
        viewModelScope.launch { thresholds.saveLateralThresholdMeters(meters) }
    }

    fun onLongitudinalThresholdChanged(meters: Double) {
        viewModelScope.launch { thresholds.saveLongitudinalThresholdMeters(meters) }
    }

    fun onResetLongitudinalThreshold() {
        viewModelScope.launch { thresholds.saveLongitudinalThresholdMeters(DEFAULT_LONGITUDINAL_THRESHOLD_METERS) }
    }

    fun onResetLateralThreshold() {
        viewModelScope.launch { thresholds.saveLateralThresholdMeters(DEFAULT_LATERAL_THRESHOLD_METERS) }
    }

    fun onSustainedApproachDurationSecondsChanged(seconds: Int) {
        viewModelScope.launch { thresholds.saveSustainedApproachDurationSeconds(seconds) }
    }

    fun onResetSustainedApproachDurationSeconds() {
        viewModelScope.launch {
            thresholds.saveSustainedApproachDurationSeconds(DEFAULT_SUSTAINED_APPROACH_DURATION_SECONDS)
        }
    }

    fun onSkipFirstLapChanged(skip: Boolean) {
        viewModelScope.launch { vehicleApproachPreferences.saveSkipFirstLap(skip) }
    }

    fun onStartReadoutEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            saveEnabledState(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout, enabled)
        }
    }

    fun onSustainedReadoutEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            saveEnabledState(ReadoutItemKey.LmuWindows.VehicleApproach.Sustained, enabled)
        }
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
        const val DEFAULT_SUSTAINED_APPROACH_DURATION_SECONDS = 7
    }
}
