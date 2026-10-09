package kurou.kodriver.feature.acewindowsreadout.vehicleapproachdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.preview.ReadoutTextPreviewHelper
import kurou.kodriver.domain.usecase.AceWindowsVehicleApproachThresholdsUseCases
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsVehicleApproachReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsVehicleApproachEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsVehicleApproachReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

/**
 * ACE 車両接近アナウンス詳細設定の ViewModel。
 *
 * 閾値（スライダー）、接近開始時読み上げの有効/無効はいずれも DataStore に永続化される。
 * ACE の共有メモリには自車の向きに相当するフィールドが存在せず、自車中心から相手車両中心までの
 * 合成距離のみが取得できるため、LMU のような前後・左右を区別した閾値・読み上げ文言の選択は持たず、
 * 単一の閾値と単一のアナウンス（[kurou.kodriver.domain.engine.SpeechEvent.AceWindowsVehicleApproach]）のみを扱う。
 */
@Suppress("LongParameterList")
internal class AceWindowsReadoutVehicleApproachDetailViewModel(
    private val thresholds: AceWindowsVehicleApproachThresholdsUseCases,
    observeEnabledStates: ObserveAceWindowsVehicleApproachEnabledStatesUseCase,
    private val saveEnabledState: SaveAceWindowsVehicleApproachEnabledStateUseCase,
    observeReadoutText: ObserveAceWindowsVehicleApproachReadoutTextUseCase,
    private val saveReadoutText: SaveAceWindowsVehicleApproachReadoutTextUseCase,
    speakText: SpeakTextUseCase,
    playStartSoundForKey: PlayStartSoundForKeyUseCase,
    checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    observeSoundVolume: ObserveSoundVolumeUseCase,
) : ViewModel() {
    private val preview =
        ReadoutTextPreviewHelper(
            viewModelScope,
            checkTextToSpeechAvailable,
            observeSoundVolume,
            playStartSoundForKey,
            speakText,
        )

    val uiState: StateFlow<AceWindowsReadoutVehicleApproachDetailUiState> =
        combine(
            thresholds.observeThresholdMeters(),
            observeEnabledStates(),
            observeReadoutText(),
            preview.textToSpeechAvailable,
        ) { thresholdMeters, enabledStates, readoutText, isTextToSpeechAvailable ->
            AceWindowsReadoutVehicleApproachDetailUiState(
                thresholdMeters = thresholdMeters,
                readoutText = readoutText,
                isTextToSpeechAvailable = isTextToSpeechAvailable,
                startReadoutEnabled = enabledStates.getValue(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout),
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AceWindowsReadoutVehicleApproachDetailUiState(),
        )

    /** ペインを離れるときに試聴を止める。 */
    fun onPreviewStopped() = preview.stop()

    fun onThresholdChanged(meters: Double) {
        viewModelScope.launch { thresholds.saveThresholdMeters(meters) }
    }

    fun onResetThreshold() {
        viewModelScope.launch { thresholds.saveThresholdMeters(ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT) }
    }

    fun onStartReadoutEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            saveEnabledState(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout, enabled)
        }
    }

    fun onReadoutTextChanged(text: String) {
        viewModelScope.launch { saveReadoutText(text) }
    }

    fun onReadoutTextReset() {
        viewModelScope.launch { saveReadoutText(ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT) }
    }

    fun onPreviewClicked(text: String) {
        preview.onPreviewClicked(text, ReadoutItemKey.AceWindows.VehicleApproach.Root)
    }
}
