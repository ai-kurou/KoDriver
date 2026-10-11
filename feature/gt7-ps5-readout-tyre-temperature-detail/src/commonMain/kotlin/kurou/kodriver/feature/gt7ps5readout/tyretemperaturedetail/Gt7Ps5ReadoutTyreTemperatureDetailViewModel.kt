package kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.formatGt7Ps5TyreTemperatureReadoutText
import kurou.kodriver.domain.preview.ReadoutTextPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5TyreTemperatureEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5TyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

/**
 * GT7 タイヤ温度アナウンス詳細設定の ViewModel。
 *
 * 高温閾値（スライダー）・過熱警告の有効/無効・文言はいずれも DataStore に永続化される。
 */
internal data class TyreTemperatureUseCases(
    val observeEnabledStates: ObserveGt7Ps5TyreTemperatureEnabledStatesUseCase,
    val observeHighThreshold: ObserveGt7Ps5TyreTemperatureHighThresholdUseCase,
    val saveEnabledState: SaveGt7Ps5TyreTemperatureEnabledStateUseCase,
    val saveHighThreshold: SaveGt7Ps5TyreTemperatureHighThresholdUseCase,
)

internal data class TyreTemperatureReadoutUseCases(
    val observeText: ObserveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase,
    val saveText: SaveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase,
    val speakText: SpeakTextUseCase,
    val playStartSoundForKey: PlayStartSoundForKeyUseCase,
    val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    val observeSoundVolume: ObserveSoundVolumeUseCase,
)

internal class Gt7Ps5ReadoutTyreTemperatureDetailViewModel(
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

    val uiState: StateFlow<Gt7Ps5ReadoutTyreTemperatureDetailUiState> =
        combine(
            tyreTemperatureUseCases.observeEnabledStates(),
            tyreTemperatureUseCases.observeHighThreshold(),
            readout.observeText(),
            preview.textToSpeechAvailable,
        ) { states, highThresholdCelsius, text, available ->
            Gt7Ps5ReadoutTyreTemperatureDetailUiState(
                overheatWarningEnabled = states.getValue(Gt7Ps5ReadoutItemKey.TyreTemperature.OverheatWarning),
                highThresholdCelsius = highThresholdCelsius.value,
                readoutText = text,
                isTextToSpeechAvailable = available,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            Gt7Ps5ReadoutTyreTemperatureDetailUiState(),
        )

    /** ペインを離れるときに試聴を止める。 */
    fun onPreviewStopped() = preview.stop()

    fun onOverheatWarningEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            tyreTemperatureUseCases.saveEnabledState(Gt7Ps5ReadoutItemKey.TyreTemperature.OverheatWarning, enabled)
        }
    }

    fun onHighThresholdChanged(celsius: Int) {
        viewModelScope.launch { tyreTemperatureUseCases.saveHighThreshold(Celsius(celsius)) }
    }

    fun onHighThresholdReset() {
        onHighThresholdChanged(GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT.value)
    }

    fun onReadoutTextChanged(text: String) {
        viewModelScope.launch { readout.saveText(text) }
    }

    /** 現在の閾値に置換し、空白文言・TTS利用不可・音量ゼロでは再生しない。 */
    fun onReadoutTextPreviewClicked(text: String) {
        val formattedText = formatGt7Ps5TyreTemperatureReadoutText(text, uiState.value.highThresholdCelsius)
        previewText(formattedText)
    }

    private fun previewText(text: String) {
        preview.onPreviewClicked(text, Gt7Ps5ReadoutItemKey.TyreTemperature.Root)
    }
}
