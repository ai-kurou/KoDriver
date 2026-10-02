package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.LmuWindowsVehicleApproachPreferencesUseCases
import kurou.kodriver.domain.usecase.LmuWindowsVehicleApproachThresholdsUseCases
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleApproachEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsVehicleApproachStartRightReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

/** 開始文言の保存とOS標準TTSでの試聴に使うUseCaseをまとめたもの。 */
internal data class StartReadoutUseCases(
    val speakText: SpeakTextUseCase,
    val playStartSoundForKey: PlayStartSoundForKeyUseCase,
    val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    val observeSoundVolume: ObserveSoundVolumeUseCase,
    val saveLeftText: SaveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase,
    val saveRightText: SaveLmuWindowsVehicleApproachStartRightReadoutTextUseCase,
)

internal class LmuWindowsReadoutVehicleApproachDetailViewModel(
    private val thresholds: LmuWindowsVehicleApproachThresholdsUseCases,
    private val vehicleApproachPreferences: LmuWindowsVehicleApproachPreferencesUseCases,
    private val observeEnabledStates: ObserveLmuWindowsVehicleApproachEnabledStatesUseCase,
    private val saveEnabledState: SaveLmuWindowsVehicleApproachEnabledStateUseCase,
    private val observeSustainedLeftText: ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase,
    private val observeSustainedRightText: ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase,
    private val startReadout: StartReadoutUseCases,
) : ViewModel() {
    private val textToSpeechAvailable =
        flow { emit(startReadout.checkTextToSpeechAvailable()) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

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
            combine(observeSustainedLeftText(), observeSustainedRightText()) { left, right -> left to right },
        ) { thresholdValues, skipFirstLap, enabledStates, startTexts, sustainedTexts ->
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
                sustainedLeftText = sustainedTexts.first,
                sustainedRightText = sustainedTexts.second,
            )
        }.combine(textToSpeechAvailable) { state, available ->
            state.copy(isTextToSpeechAvailable = available)
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

    fun onStartLeftTextChanged(text: String) {
        viewModelScope.launch { startReadout.saveLeftText(text) }
    }

    fun onStartRightTextChanged(text: String) {
        viewModelScope.launch { startReadout.saveRightText(text) }
    }

    fun onStartLeftTextPreviewClicked(text: String) {
        playStartReadoutPreview(text)
    }

    fun onStartRightTextPreviewClicked(text: String) {
        playStartReadoutPreview(text)
    }

    /** 空白文言・TTS利用不可・音量ゼロでは試聴せず、実際の読み上げと同じRootキーで開始音を鳴らす。 */
    private fun playStartReadoutPreview(text: String) {
        if (text.isBlank() || !textToSpeechAvailable.value) return
        viewModelScope.launch {
            val volume = startReadout.observeSoundVolume().first()
            if (volume <= 0) return@launch
            startReadout.playStartSoundForKey(ReadoutItemKey.LmuWindows.VehicleApproach.Root)
            startReadout.speakText(text, volume = volume)
        }
    }
}
