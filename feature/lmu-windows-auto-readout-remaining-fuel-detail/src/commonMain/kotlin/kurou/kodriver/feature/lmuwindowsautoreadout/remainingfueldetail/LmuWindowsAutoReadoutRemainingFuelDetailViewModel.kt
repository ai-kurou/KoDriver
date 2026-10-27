package kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsRemainingFuelReadoutText
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsRemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.preview.ReadoutTextPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAutoReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveAutoReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

internal data class RemainingFuelUseCases(
    val observeThresholdPercentage: ObserveLmuWindowsRemainingFuelThresholdPercentageUseCase,
    val saveThresholdPercentage: SaveLmuWindowsRemainingFuelThresholdPercentageUseCase,
    val observeAutoReadoutEnabledStates: ObserveAutoReadoutEnabledStatesUseCase,
    val saveAutoReadoutEnabledState: SaveAutoReadoutEnabledStateUseCase,
)

internal data class RemainingFuelReadoutUseCases(
    val observeText: ObserveLmuWindowsRemainingFuelReadoutTextUseCase,
    val saveText: SaveLmuWindowsRemainingFuelReadoutTextUseCase,
    val speakText: SpeakTextUseCase,
    val playStartSoundForKey: PlayStartSoundForKeyUseCase,
    val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    val observeSoundVolume: ObserveSoundVolumeUseCase,
)

internal class LmuWindowsAutoReadoutRemainingFuelDetailViewModel(
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

    val uiState: StateFlow<LmuWindowsAutoReadoutRemainingFuelDetailUiState> =
        LmuWindowsAutoReadoutRemainingFuelDetailFlow(
            thresholdPercentages = remainingFuelUseCases.observeThresholdPercentage(),
            enabledStates = remainingFuelUseCases.observeAutoReadoutEnabledStates(Simulator.LmuWindows.id),
            readoutTexts = readout.observeText(),
            isTextToSpeechAvailable = preview.textToSpeechAvailable,
        ).uiState.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsAutoReadoutRemainingFuelDetailUiState(),
        )

    /** ペインを離れるときに試聴を止める。 */
    fun onPreviewStopped() = preview.stop()

    fun onReadoutTextChanged(text: String) {
        viewModelScope.launch { readout.saveText(text) }
    }

    /** 現在の閾値に置換し、空白文言・TTS利用不可・音量ゼロでは再生しない。 */
    fun onReadoutTextPreviewClicked(text: String) {
        val formattedText = formatLmuWindowsRemainingFuelReadoutText(text, uiState.value.thresholdPercentage)
        preview.onPreviewClicked(formattedText, LmuWindowsReadoutItemKey.RemainingFuel.Root)
    }

    fun onThresholdChanged(percentage: Int) {
        viewModelScope.launch { remainingFuelUseCases.saveThresholdPercentage(percentage) }
    }

    fun onThresholdReset() {
        viewModelScope.launch {
            remainingFuelUseCases.saveThresholdPercentage(
                LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT,
            )
        }
    }

    fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            remainingFuelUseCases.saveAutoReadoutEnabledState(
                Simulator.LmuWindows.id,
                LmuWindowsReadoutItemKey.RemainingFuel.WarningReadout,
                enabled,
            )
        }
    }
}
