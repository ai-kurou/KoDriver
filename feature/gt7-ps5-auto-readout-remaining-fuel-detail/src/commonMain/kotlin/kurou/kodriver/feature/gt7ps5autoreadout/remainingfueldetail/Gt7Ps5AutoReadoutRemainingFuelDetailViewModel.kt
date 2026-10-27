package kurou.kodriver.feature.gt7ps5autoreadout.remainingfueldetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.formatGt7Ps5RemainingFuelReadoutText
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5RemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.gt7ps5.usecase.SaveGt7Ps5RemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.gt7ps5.usecase.SaveGt7Ps5RemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.preview.ReadoutTextPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAutoReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveAutoReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

internal data class RemainingFuelUseCases(
    val observeThresholdPercentage: ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase,
    val saveThresholdPercentage: SaveGt7Ps5RemainingFuelThresholdPercentageUseCase,
    val observeAutoReadoutEnabledStates: ObserveAutoReadoutEnabledStatesUseCase,
    val saveAutoReadoutEnabledState: SaveAutoReadoutEnabledStateUseCase,
)

internal data class RemainingFuelReadoutUseCases(
    val observeText: ObserveGt7Ps5RemainingFuelReadoutTextUseCase,
    val saveText: SaveGt7Ps5RemainingFuelReadoutTextUseCase,
    val speakText: SpeakTextUseCase,
    val playStartSoundForKey: PlayStartSoundForKeyUseCase,
    val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    val observeSoundVolume: ObserveSoundVolumeUseCase,
)

internal class Gt7Ps5AutoReadoutRemainingFuelDetailViewModel(
    private val remainingFuelUseCases: RemainingFuelUseCases,
    private val readout: RemainingFuelReadoutUseCases,
) : ViewModel() {
    private val preview =
        ReadoutTextPreviewHelper(
            viewModelScope,
            readout.checkTextToSpeechAvailable,
            readout.observeSoundVolume,
            readout.playStartSoundForKey,
            readout.speakText,
        )

    val uiState: StateFlow<Gt7Ps5AutoReadoutRemainingFuelDetailUiState> =
        Gt7Ps5AutoReadoutRemainingFuelDetailFlow(
            thresholdPercentage = remainingFuelUseCases.observeThresholdPercentage(),
            enabledStates = remainingFuelUseCases.observeAutoReadoutEnabledStates(Simulator.Gt7Ps5.id),
            readoutText = readout.observeText(),
            isTextToSpeechAvailable = preview.textToSpeechAvailable,
        ).uiState.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            Gt7Ps5AutoReadoutRemainingFuelDetailUiState(),
        )

    /** ペインを離れるときに試聴を止める。 */
    fun onPreviewStopped() = preview.stop()

    fun onReadoutTextChanged(text: String) {
        viewModelScope.launch { readout.saveText(text) }
    }

    /** 現在の閾値に置換し、空白文言・TTS利用不可・音量ゼロでは再生しない。 */
    fun onReadoutTextPreviewClicked(text: String) {
        val formattedText = formatGt7Ps5RemainingFuelReadoutText(text, uiState.value.thresholdPercentage)
        previewText(formattedText)
    }

    private fun previewText(text: String) {
        preview.onPreviewClicked(text, Gt7Ps5ReadoutItemKey.RemainingFuel.Root)
    }

    fun onThresholdChanged(percentage: Int) {
        viewModelScope.launch { remainingFuelUseCases.saveThresholdPercentage(percentage) }
    }

    fun onThresholdReset() {
        onThresholdChanged(GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT)
    }

    fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            remainingFuelUseCases.saveAutoReadoutEnabledState(
                Simulator.Gt7Ps5.id,
                Gt7Ps5ReadoutItemKey.RemainingFuel.DetailEnabled,
                enabled,
            )
        }
    }
}
