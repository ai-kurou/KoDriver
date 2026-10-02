package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.VehicleApproachSustainedReadoutType
import kurou.kodriver.domain.usecase.LmuWindowsVehicleApproachPreferencesUseCases
import kurou.kodriver.domain.usecase.LmuWindowsVehicleApproachThresholdsUseCases
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleApproachEnabledStateUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

internal class LmuWindowsReadoutVehicleApproachDetailViewModel(
    private val thresholds: LmuWindowsVehicleApproachThresholdsUseCases,
    private val vehicleApproachPreferences: LmuWindowsVehicleApproachPreferencesUseCases,
    private val observeEnabledStates: ObserveLmuWindowsVehicleApproachEnabledStatesUseCase,
    private val saveEnabledState: SaveLmuWindowsVehicleApproachEnabledStateUseCase,
    private val playSpeechEvent: PlaySpeechEventUseCase,
    private val speakText: SpeakTextUseCase,
) : ViewModel() {
    val uiState: StateFlow<LmuWindowsReadoutVehicleApproachDetailUiState> =
        combine(
            combine(
                thresholds.observeLateralThresholdMeters(),
                thresholds.observeLongitudinalThresholdMeters(),
                thresholds.observeSustainedApproachDurationSeconds(),
            ) { lateral, longitudinal, sustainedDuration -> Triple(lateral, longitudinal, sustainedDuration) },
            vehicleApproachPreferences.observeSkipFirstLap(),
            observeEnabledStates(),
            combine(
                vehicleApproachPreferences.observeStartLeftReadoutText(),
                vehicleApproachPreferences.observeStartRightReadoutText(),
            ) { left, right -> left to right },
            vehicleApproachPreferences.observeSustainedReadoutType(),
        ) { thresholdValues, skipFirstLap, enabledStates, startTexts, sustainedReadoutType ->
            val (lateral, longitudinal, sustainedDuration) = thresholdValues
            LmuWindowsReadoutVehicleApproachDetailUiState(
                lateralThresholdMeters = lateral,
                longitudinalThresholdMeters = longitudinal,
                sustainedApproachDurationSeconds = sustainedDuration,
                skipFirstLap = skipFirstLap,
                startReadoutEnabled = enabledStates.getValue(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout),
                startLeftText = startTexts.first,
                startRightText = startTexts.second,
                sustainedReadoutEnabled = enabledStates.getValue(ReadoutItemKey.LmuWindows.VehicleApproach.Sustained),
                sustainedReadoutType = sustainedReadoutType,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsReadoutVehicleApproachDetailUiState(),
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
            saveEnabledState(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout, enabled)
        }
    }

    fun onSustainedReadoutEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            saveEnabledState(ReadoutItemKey.LmuWindows.VehicleApproach.Sustained, enabled)
        }
    }

    fun onSustainedReadoutTypeChanged(type: VehicleApproachSustainedReadoutType) {
        viewModelScope.launch { vehicleApproachPreferences.saveSustainedReadoutType(type) }
        playSustainedReadoutPreview(type)
    }

    fun onStartReadoutPreviewClicked() {
        viewModelScope.launch {
            val left = vehicleApproachPreferences.observeStartLeftReadoutText().first()
            val right = vehicleApproachPreferences.observeStartRightReadoutText().first()
            speakText(left)
            speakText(right, queue = left.isNotBlank())
        }
    }

    private fun playSustainedReadoutPreview(type: VehicleApproachSustainedReadoutType) {
        val events =
            when (type) {
                VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT -> {
                    SpeechEvent.KeepLeft to SpeechEvent.KeepRight
                }

                VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED -> {
                    SpeechEvent.LeftSustained to SpeechEvent.RightSustained
                }
            }
        playSpeechEvent(events.first)
        playSpeechEvent(events.second, queue = true)
    }
}
