package kurou.kodriver.feature.acewindowsautoreadout.remainingfuellapsdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT
import kurou.kodriver.domain.acewindows.model.formatAceWindowsRemainingFuelLapsReadoutText
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelLapsThresholdUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsRemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsRemainingFuelLapsThresholdUseCase
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.readoutEnabled
import kurou.kodriver.domain.preview.ReadoutTextPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAutoReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveAutoReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

internal data class RemainingFuelLapsUseCases(
    val observeThreshold: ObserveAceWindowsRemainingFuelLapsThresholdUseCase,
    val saveThreshold: SaveAceWindowsRemainingFuelLapsThresholdUseCase,
    val observeAutoReadoutEnabledStates: ObserveAutoReadoutEnabledStatesUseCase,
    val saveAutoReadoutEnabledState: SaveAutoReadoutEnabledStateUseCase,
)

internal data class RemainingFuelLapsReadoutUseCases(
    val observeText: ObserveAceWindowsRemainingFuelLapsReadoutTextUseCase,
    val saveText: SaveAceWindowsRemainingFuelLapsReadoutTextUseCase,
    val observeEmptyText: ObserveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase,
    val saveEmptyText: SaveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase,
    val speakText: SpeakTextUseCase,
    val playStartSoundForKey: PlayStartSoundForKeyUseCase,
    val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    val observeSoundVolume: ObserveSoundVolumeUseCase,
)

internal class AceWindowsAutoReadoutRemainingFuelLapsDetailViewModel(
    private val remainingFuelLapsUseCases: RemainingFuelLapsUseCases,
    private val readout: RemainingFuelLapsReadoutUseCases,
) : ViewModel() {
    private val preview =
        ReadoutTextPreviewHelper(
            viewModelScope,
            readout.checkTextToSpeechAvailable,
            readout.observeSoundVolume,
            readout.playStartSoundForKey,
            readout.speakText,
        )

    val uiState: StateFlow<AceWindowsAutoReadoutRemainingFuelLapsDetailUiState> =
        combine(
            remainingFuelLapsUseCases.observeThreshold(),
            remainingFuelLapsUseCases.observeAutoReadoutEnabledStates(Simulator.AceWindows.id),
            readout.observeText(),
            readout.observeEmptyText(),
            preview.textToSpeechAvailable,
        ) { remainingFuelLaps, enabledStates, text, emptyText, available ->
            AceWindowsAutoReadoutRemainingFuelLapsDetailUiState(
                remainingFuelLaps = remainingFuelLaps,
                readoutText = text,
                emptyReadoutText = emptyText,
                isTextToSpeechAvailable = available,
                enabled = enabledStates.readoutEnabled(AceWindowsReadoutItemKey.RemainingFuelLaps.DetailEnabled),
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AceWindowsAutoReadoutRemainingFuelLapsDetailUiState(),
        )

    /** ペインを離れるときに試聴を止める。 */
    fun onPreviewStopped() = preview.stop()

    fun onReadoutTextChanged(text: String) {
        viewModelScope.launch { readout.saveText(text) }
    }

    /** 画面に表示中の閾値に置換し、空白文言・TTS利用不可・音量ゼロでは再生しない。 */
    fun onReadoutTextPreviewClicked(
        text: String,
        remainingFuelLaps: Int,
    ) {
        val formattedText = formatAceWindowsRemainingFuelLapsReadoutText(text, remainingFuelLaps)
        previewText(formattedText)
    }

    fun onEmptyReadoutTextChanged(text: String) {
        viewModelScope.launch { readout.saveEmptyText(text) }
    }

    fun onEmptyReadoutTextPreviewClicked(text: String) {
        previewText(text)
    }

    private fun previewText(text: String) {
        preview.onPreviewClicked(text, AceWindowsReadoutItemKey.RemainingFuelLaps.Root)
    }

    fun onRemainingFuelLapsChanged(laps: Int) {
        viewModelScope.launch { remainingFuelLapsUseCases.saveThreshold(laps) }
    }

    fun onResetRemainingFuelLaps() {
        onRemainingFuelLapsChanged(ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT)
    }

    fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            remainingFuelLapsUseCases.saveAutoReadoutEnabledState(
                Simulator.AceWindows.id,
                AceWindowsReadoutItemKey.RemainingFuelLaps.DetailEnabled,
                enabled,
            )
        }
    }
}
