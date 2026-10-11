package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.formatGt7Ps5RemainingFuelLapsReadoutText
import kurou.kodriver.domain.model.readoutEnabled
import kurou.kodriver.domain.preview.ReadoutTextPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

internal data class RemainingFuelLapsUseCases(
    val observeRemainingFuelLaps: ObserveGt7Ps5RemainingFuelLapsUseCase,
    val saveRemainingFuelLaps: SaveGt7Ps5RemainingFuelLapsUseCase,
    val observeReadoutEnabledStates: ObserveReadoutEnabledStatesUseCase,
    val saveReadoutEnabledState: SaveReadoutEnabledStateUseCase,
)

internal data class RemainingFuelLapsReadoutUseCases(
    val observeText: ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCase,
    val saveText: SaveGt7Ps5RemainingFuelLapsReadoutTextUseCase,
    val observeEmptyText: ObserveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase,
    val saveEmptyText: SaveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase,
    val speakText: SpeakTextUseCase,
    val playStartSoundForKey: PlayStartSoundForKeyUseCase,
    val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    val observeSoundVolume: ObserveSoundVolumeUseCase,
)

internal class Gt7Ps5ReadoutRemainingFuelLapsDetailViewModel(
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

    val uiState: StateFlow<Gt7Ps5ReadoutRemainingFuelLapsDetailUiState> =
        combine(
            remainingFuelLapsUseCases.observeRemainingFuelLaps(),
            remainingFuelLapsUseCases.observeReadoutEnabledStates(Simulator.Gt7Ps5.id),
            readout.observeText(),
            readout.observeEmptyText(),
            preview.textToSpeechAvailable,
        ) { remainingFuelLaps, enabledStates, text, emptyText, available ->
            Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(
                remainingFuelLaps = remainingFuelLaps,
                readoutText = text,
                emptyReadoutText = emptyText,
                isTextToSpeechAvailable = available,
                enabled = enabledStates.readoutEnabled(Gt7Ps5ReadoutItemKey.RemainingFuelLaps.DetailEnabled),
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(),
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
        val formattedText = formatGt7Ps5RemainingFuelLapsReadoutText(text, remainingFuelLaps)
        previewText(formattedText)
    }

    fun onEmptyReadoutTextChanged(text: String) {
        viewModelScope.launch { readout.saveEmptyText(text) }
    }

    fun onEmptyReadoutTextPreviewClicked(text: String) {
        previewText(text)
    }

    private fun previewText(text: String) {
        preview.onPreviewClicked(text, Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root)
    }

    fun onRemainingFuelLapsChanged(laps: Int) {
        viewModelScope.launch { remainingFuelLapsUseCases.saveRemainingFuelLaps(laps) }
    }

    fun onResetRemainingFuelLaps() {
        viewModelScope.launch {
            remainingFuelLapsUseCases.saveRemainingFuelLaps(
                GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT,
            )
        }
    }

    fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            remainingFuelLapsUseCases.saveReadoutEnabledState(
                Simulator.Gt7Ps5.id,
                Gt7Ps5ReadoutItemKey.RemainingFuelLaps.DetailEnabled,
                enabled,
            )
        }
    }
}
