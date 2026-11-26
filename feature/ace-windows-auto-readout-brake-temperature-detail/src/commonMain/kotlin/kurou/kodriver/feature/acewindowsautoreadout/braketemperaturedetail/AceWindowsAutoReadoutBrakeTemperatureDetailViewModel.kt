package kurou.kodriver.feature.acewindowsautoreadout.braketemperaturedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.acewindows.model.formatAceWindowsBrakeTemperatureReadoutText
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsBrakeTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsBrakeTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsBrakeTemperatureEnabledStateUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsBrakeTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.launchCapturingErrors
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.preview.ReadoutTextPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

internal data class BrakeTemperatureUseCases(
    val observeEnabledStates: ObserveAceWindowsBrakeTemperatureEnabledStatesUseCase,
    val observeHighThreshold: ObserveAceWindowsBrakeTemperatureHighThresholdUseCase,
    val saveEnabledState: SaveAceWindowsBrakeTemperatureEnabledStateUseCase,
    val saveHighThreshold: SaveAceWindowsBrakeTemperatureHighThresholdUseCase,
)

internal data class BrakeTemperatureReadoutUseCases(
    val observeText: ObserveAceWindowsBrakeTemperatureOverheatReadoutTextUseCase,
    val saveText: SaveAceWindowsBrakeTemperatureOverheatReadoutTextUseCase,
    val speakText: SpeakTextUseCase,
    val playStartSoundForKey: PlayStartSoundForKeyUseCase,
    val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    val observeSoundVolume: ObserveSoundVolumeUseCase,
)

/**
 * ACE ブレーキ温度アナウンス詳細設定の ViewModel。
 *
 * 過熱閾値・過熱警告の有効/無効・読み上げ文言を DataStore に永続化する。
 */
internal class AceWindowsAutoReadoutBrakeTemperatureDetailViewModel(
    private val brakeTemperatureUseCases: BrakeTemperatureUseCases,
    private val readout: BrakeTemperatureReadoutUseCases,
) : ViewModel() {
    private val preview =
        ReadoutTextPreviewHelper(
            viewModelScope,
            readout.checkTextToSpeechAvailable,
            readout.observeSoundVolume,
            readout.playStartSoundForKey,
            readout.speakText,
        )

    val uiState: StateFlow<AceWindowsAutoReadoutBrakeTemperatureDetailUiState> =
        AceWindowsAutoReadoutBrakeTemperatureDetailFlow(
            enabledStates = brakeTemperatureUseCases.observeEnabledStates(),
            highThresholdCelsius = brakeTemperatureUseCases.observeHighThreshold(),
            overheatReadoutText = readout.observeText(),
            isTextToSpeechAvailable = preview.textToSpeechAvailable,
        ).uiState.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AceWindowsAutoReadoutBrakeTemperatureDetailUiState(),
        )

    /** ペインを離れるときに試聴を止める。 */
    fun onPreviewStopped() = preview.stop()

    fun onOverheatWarningEnabledChanged(enabled: Boolean) {
        viewModelScope.launchCapturingErrors {
            brakeTemperatureUseCases.saveEnabledState(
                AceWindowsReadoutItemKey.BrakeTemperature.OverheatWarning,
                enabled,
            )
        }
    }

    fun onHighThresholdChanged(celsius: Int) {
        viewModelScope.launchCapturingErrors { brakeTemperatureUseCases.saveHighThreshold(Celsius(celsius)) }
    }

    fun onHighThresholdReset() {
        onHighThresholdChanged(ACE_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value)
    }

    fun onOverheatReadoutTextChanged(text: String) {
        viewModelScope.launchCapturingErrors { readout.saveText(text) }
    }

    /** 保存反映待ちの旧値を避けるため、画面に表示中の過熱閾値を呼び出し側から受け取る。 */
    fun onOverheatReadoutTextPreviewClicked(
        text: String,
        celsius: Int,
    ) {
        val resolvedText = formatAceWindowsBrakeTemperatureReadoutText(text, celsius)
        preview.onPreviewClicked(resolvedText, AceWindowsReadoutItemKey.BrakeTemperature.Root)
    }
}
