package kurou.kodriver.feature.lmuwindowsautoreadout.vehicleapproachdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT
import kurou.kodriver.domain.lmuwindows.usecase.LmuWindowsVehicleApproachPreferencesUseCases
import kurou.kodriver.domain.lmuwindows.usecase.LmuWindowsVehicleApproachThresholdsUseCases
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsVehicleApproachEnabledStateUseCase
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey

internal class LmuWindowsAutoReadoutVehicleApproachDetailViewModel(
    private val thresholds: LmuWindowsVehicleApproachThresholdsUseCases,
    private val vehicleApproachPreferences: LmuWindowsVehicleApproachPreferencesUseCases,
    private val observeEnabledStates: ObserveLmuWindowsVehicleApproachEnabledStatesUseCase,
    private val saveEnabledState: SaveLmuWindowsVehicleApproachEnabledStateUseCase,
    readoutTextUseCases: ReadoutTextSettingsUseCases,
) : ViewModel() {
    val readoutTextSettings =
        ReadoutTextSettingsStateHolder(
            viewModelScope,
            readoutTextUseCases.vehicleApproachPreferences,
            readoutTextUseCases.observeSustainedLeftText,
            readoutTextUseCases.observeSustainedRightText,
            readoutTextUseCases.saveStartLeftText,
            readoutTextUseCases.saveStartRightText,
            readoutTextUseCases.checkTextToSpeechAvailable,
            readoutTextUseCases.observeSoundVolume,
            readoutTextUseCases.playStartSoundForKey,
            readoutTextUseCases.speakText,
        )
    val uiState: StateFlow<LmuWindowsAutoReadoutVehicleApproachDetailUiState> =
        combine(
            combine(
                thresholds.observeLateralThresholdMeters(),
                thresholds.observeLongitudinalThresholdMeters(),
                thresholds.observeSustainedApproachDurationSeconds(),
            ) { lateral, longitudinal, duration -> Triple(lateral, longitudinal, duration) },
            vehicleApproachPreferences.observeSkipFirstLap(),
            observeEnabledStates(),
            readoutTextSettings.uiState,
        ) { thresholdValues, skipFirstLap, enabledStates, textState ->
            val (lateral, longitudinal, duration) = thresholdValues
            LmuWindowsAutoReadoutVehicleApproachDetailUiState(
                lateralThresholdMeters = lateral,
                longitudinalThresholdMeters = longitudinal,
                sustainedApproachDurationSeconds = duration,
                skipFirstLap = skipFirstLap,
                startReadoutEnabled = enabledStates.getValue(LmuWindowsReadoutItemKey.VehicleApproach.StartReadout),
                startLeftText = textState.startLeftText,
                startRightText = textState.startRightText,
                sustainedReadoutEnabled = enabledStates.getValue(LmuWindowsReadoutItemKey.VehicleApproach.Sustained),
                sustainedLeftText = textState.sustainedLeftText,
                sustainedRightText = textState.sustainedRightText,
                isTextToSpeechAvailable = textState.isTextToSpeechAvailable,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsAutoReadoutVehicleApproachDetailUiState(),
        )

    fun onLateralThresholdChanged(meters: Double) {
        viewModelScope.launch { thresholds.saveLateralThresholdMeters(meters) }
    }

    fun onLongitudinalThresholdChanged(meters: Double) {
        viewModelScope.launch { thresholds.saveLongitudinalThresholdMeters(meters) }
    }

    fun onResetLongitudinalThreshold() {
        viewModelScope.launch {
            thresholds.saveLongitudinalThresholdMeters(
                LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT,
            )
        }
    }

    fun onResetLateralThreshold() {
        viewModelScope.launch {
            thresholds.saveLateralThresholdMeters(LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT)
        }
    }

    fun onSustainedApproachDurationSecondsChanged(seconds: Int) {
        viewModelScope.launch { thresholds.saveSustainedApproachDurationSeconds(seconds) }
    }

    fun onResetSustainedApproachDurationSeconds() {
        viewModelScope.launch {
            thresholds.saveSustainedApproachDurationSeconds(
                LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT,
            )
        }
    }

    fun onSkipFirstLapChanged(skip: Boolean) {
        viewModelScope.launch { vehicleApproachPreferences.saveSkipFirstLap(skip) }
    }

    fun onStartReadoutEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            saveEnabledState(LmuWindowsReadoutItemKey.VehicleApproach.StartReadout, enabled)
        }
    }

    fun onSustainedReadoutEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            saveEnabledState(LmuWindowsReadoutItemKey.VehicleApproach.Sustained, enabled)
        }
    }
}
