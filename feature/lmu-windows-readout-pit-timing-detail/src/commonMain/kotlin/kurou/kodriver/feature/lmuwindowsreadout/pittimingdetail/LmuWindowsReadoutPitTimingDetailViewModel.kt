package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.formatLmuWindowsPitTimingReadoutText
import kurou.kodriver.domain.preview.ReadoutTextPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingTyreWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

private const val PIT_TIMING_PREVIEW_LAPS = 5

internal data class PitTimingUseCases(
    val observeVirtualEnergyLaps: ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase,
    val observeTyreWearLaps: ObserveLmuWindowsPitTimingTyreWearLapsUseCase,
    val observeEnabledStates: ObserveLmuWindowsPitTimingEnabledStatesUseCase,
    val saveVirtualEnergyLaps: SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase,
    val saveTyreWearLaps: SaveLmuWindowsPitTimingTyreWearLapsUseCase,
    val saveEnabledState: SaveLmuWindowsPitTimingEnabledStateUseCase,
)

/** 文言の監視・保存とOS標準TTSでの試聴に使うUseCaseをまとめる。 */
@Suppress("LongParameterList")
internal data class PitTimingReadoutUseCases(
    val observeText: ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase,
    val observeImminentText: ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase,
    val saveText: SaveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase,
    val saveImminentText: SaveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase,
    val observeTyreWearText: ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase,
    val observeTyreWearImminentText: ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase,
    val saveTyreWearText: SaveLmuWindowsPitTimingTyreWearReadoutTextUseCase,
    val saveTyreWearImminentText: SaveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase,
    val speakText: SpeakTextUseCase,
    val playStartSoundForKey: PlayStartSoundForKeyUseCase,
    val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    val observeSoundVolume: ObserveSoundVolumeUseCase,
)

internal class LmuWindowsReadoutPitTimingDetailViewModel(
    private val pitTimingUseCases: PitTimingUseCases,
    private val readout: PitTimingReadoutUseCases,
) : ViewModel() {
    private val preview =
        ReadoutTextPreviewHelper(
            viewModelScope,
            readout.checkTextToSpeechAvailable,
            readout.observeSoundVolume,
            readout.playStartSoundForKey,
            readout.speakText,
        )

    val uiState: StateFlow<LmuWindowsReadoutPitTimingDetailUiState> =
        combine(
            pitTimingUseCases.observeVirtualEnergyLaps(),
            pitTimingUseCases.observeTyreWearLaps(),
            pitTimingUseCases.observeEnabledStates(),
            combine(
                combine(readout.observeText(), readout.observeImminentText()) { text, imminent -> text to imminent },
                combine(readout.observeTyreWearText(), readout.observeTyreWearImminentText()) { text, imminent ->
                    text to imminent
                },
            ) { virtualEnergy, tyreWear -> virtualEnergy to tyreWear },
            preview.textToSpeechAvailable,
        ) { virtualEnergyLaps, tyreWearLaps, enabledStates, texts, available ->
            LmuWindowsReadoutPitTimingDetailUiState(
                virtualEnergyEnabled = enabledStates.getValue(ReadoutItemKey.LmuWindows.PitTiming.VirtualEnergy),
                virtualEnergyLaps = virtualEnergyLaps,
                virtualEnergyText = texts.first.first,
                virtualEnergyImminentText = texts.first.second,
                isTextToSpeechAvailable = available,
                tyreWearEnabled = enabledStates.getValue(ReadoutItemKey.LmuWindows.PitTiming.TyreWear),
                tyreWearLaps = tyreWearLaps,
                tyreWearText = texts.second.first,
                tyreWearImminentText = texts.second.second,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsReadoutPitTimingDetailUiState(),
        )

    /** ペインを離れるときに試聴を止める。 */
    fun onPreviewStopped() = preview.stop()

    fun onVirtualEnergyEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            pitTimingUseCases.saveEnabledState(ReadoutItemKey.LmuWindows.PitTiming.VirtualEnergy, enabled)
        }
    }

    fun onVirtualEnergyLapsChanged(laps: Int) {
        viewModelScope.launch {
            pitTimingUseCases.saveVirtualEnergyLaps(laps)
        }
    }

    fun onTyreWearEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            pitTimingUseCases.saveEnabledState(ReadoutItemKey.LmuWindows.PitTiming.TyreWear, enabled)
        }
    }

    fun onTyreWearLapsChanged(laps: Int) {
        viewModelScope.launch {
            pitTimingUseCases.saveTyreWearLaps(laps)
        }
    }

    fun onVirtualEnergyTextChanged(text: String) {
        viewModelScope.launch { readout.saveText(text) }
    }

    fun onVirtualEnergyImminentTextChanged(text: String) {
        viewModelScope.launch { readout.saveImminentText(text) }
    }

    fun onVirtualEnergyTextPreviewClicked(text: String) {
        playReadoutPreview(formatLmuWindowsPitTimingReadoutText(text, PIT_TIMING_PREVIEW_LAPS))
    }

    fun onVirtualEnergyImminentTextPreviewClicked(text: String) {
        playReadoutPreview(text)
    }

    fun onTyreWearTextChanged(text: String) {
        viewModelScope.launch { readout.saveTyreWearText(text) }
    }

    fun onTyreWearImminentTextChanged(text: String) {
        viewModelScope.launch { readout.saveTyreWearImminentText(text) }
    }

    fun onTyreWearTextPreviewClicked(text: String) {
        playReadoutPreview(formatLmuWindowsPitTimingReadoutText(text, PIT_TIMING_PREVIEW_LAPS))
    }

    fun onTyreWearImminentTextPreviewClicked(text: String) {
        playReadoutPreview(text)
    }

    /** 空白文言・TTS利用不可・音量ゼロでは開始音も本文も再生しない。 */
    private fun playReadoutPreview(text: String) {
        preview.onPreviewClicked(text, ReadoutItemKey.LmuWindows.PitTiming.Root)
    }
}
