package kurou.kodriver.feature.acewindowsreadout.tyretemperaturedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.formatAceWindowsTyreTemperatureReadoutText
import kurou.kodriver.domain.preview.ReadoutTextPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsTyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

internal data class TyreTemperatureUseCases(
    val observeEnabledStates: ObserveAceWindowsTyreTemperatureEnabledStatesUseCase,
    val observeHighThreshold: ObserveAceWindowsTyreTemperatureHighThresholdUseCase,
    val saveEnabledState: SaveAceWindowsTyreTemperatureEnabledStateUseCase,
    val saveHighThreshold: SaveAceWindowsTyreTemperatureHighThresholdUseCase,
)

internal data class TyreTemperatureReadoutUseCases(
    val observeText: ObserveAceWindowsTyreTemperatureOverheatReadoutTextUseCase,
    val saveText: SaveAceWindowsTyreTemperatureOverheatReadoutTextUseCase,
    val speakText: SpeakTextUseCase,
    val playStartSoundForKey: PlayStartSoundForKeyUseCase,
    val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    val observeSoundVolume: ObserveSoundVolumeUseCase,
)

/**
 * ACE タイヤ温度アナウンス詳細設定の ViewModel。
 *
 * 高温閾値・過熱警告の有効/無効・読み上げ文言を DataStore に永続化する。
 */
internal class AceWindowsReadoutTyreTemperatureDetailViewModel(
    private val tyreTemperatureUseCases: TyreTemperatureUseCases,
    private val readout: TyreTemperatureReadoutUseCases,
) : ViewModel() {
    private val preview =
        ReadoutTextPreviewHelper(
            viewModelScope,
            readout.checkTextToSpeechAvailable,
            readout.observeSoundVolume,
            readout.playStartSoundForKey,
            readout.speakText,
        )

    val uiState: StateFlow<AceWindowsReadoutTyreTemperatureDetailUiState> =
        combine(
            tyreTemperatureUseCases.observeEnabledStates(),
            tyreTemperatureUseCases.observeHighThreshold(),
            readout.observeText(),
            preview.textToSpeechAvailable,
        ) { states, highThresholdCelsius, text, available ->
            AceWindowsReadoutTyreTemperatureDetailUiState(
                overheatWarningEnabled = states.getValue(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning),
                highThresholdCelsius = highThresholdCelsius.value,
                overheatReadoutText = text,
                isTextToSpeechAvailable = available,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AceWindowsReadoutTyreTemperatureDetailUiState(),
        )

    /** ペインを離れるときに試聴を止める。 */
    fun onPreviewStopped() = preview.stop()

    fun onOverheatWarningEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            tyreTemperatureUseCases.saveEnabledState(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning, enabled)
        }
    }

    fun onHighThresholdChanged(celsius: Int) {
        viewModelScope.launch { tyreTemperatureUseCases.saveHighThreshold(Celsius(celsius)) }
    }

    fun onHighThresholdReset() {
        onHighThresholdChanged(ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value)
    }

    fun onOverheatReadoutTextChanged(text: String) {
        viewModelScope.launch { readout.saveText(text) }
    }

    /** 保存反映待ちの旧値を避けるため、画面に表示中の高温閾値を呼び出し側から受け取る。 */
    fun onOverheatReadoutTextPreviewClicked(
        text: String,
        celsius: Int,
    ) {
        val resolvedText = formatAceWindowsTyreTemperatureReadoutText(text, celsius)
        preview.onPreviewClicked(resolvedText, ReadoutItemKey.AceWindows.TyreTemperature.Root)
    }
}
