package kurou.kodriver.feature.lmuwindowsautoreadout.pittimingdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsPitTimingReadoutText
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingFuelImminentReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingFuelLapsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingFuelReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsPitTimingEnabledStateUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsPitTimingFuelImminentReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsPitTimingFuelLapsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsPitTimingFuelReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsPitTimingTyreWearReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.preview.ReadoutTextPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

internal data class PitTimingUseCases(
    val observeVirtualEnergyLaps: ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase,
    val observeTyreWearLaps: ObserveLmuWindowsPitTimingTyreWearLapsUseCase,
    val observeFuelLaps: ObserveLmuWindowsPitTimingFuelLapsUseCase,
    val observeEnabledStates: ObserveLmuWindowsPitTimingEnabledStatesUseCase,
    val saveVirtualEnergyLaps: SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase,
    val saveTyreWearLaps: SaveLmuWindowsPitTimingTyreWearLapsUseCase,
    val saveFuelLaps: SaveLmuWindowsPitTimingFuelLapsUseCase,
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
    val observeFuelText: ObserveLmuWindowsPitTimingFuelReadoutTextUseCase,
    val observeTyreWearImminentText: ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase,
    val observeFuelImminentText: ObserveLmuWindowsPitTimingFuelImminentReadoutTextUseCase,
    val saveTyreWearText: SaveLmuWindowsPitTimingTyreWearReadoutTextUseCase,
    val saveFuelText: SaveLmuWindowsPitTimingFuelReadoutTextUseCase,
    val saveTyreWearImminentText: SaveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase,
    val saveFuelImminentText: SaveLmuWindowsPitTimingFuelImminentReadoutTextUseCase,
    val speakText: SpeakTextUseCase,
    val playStartSoundForKey: PlayStartSoundForKeyUseCase,
    val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    val observeSoundVolume: ObserveSoundVolumeUseCase,
)

@Suppress("TooManyFunctions")
internal class LmuWindowsAutoReadoutPitTimingDetailViewModel(
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

    val uiState: StateFlow<LmuWindowsAutoReadoutPitTimingDetailUiState> =
        combine(
            pitTimingUseCases.observeVirtualEnergyLaps(),
            combine(pitTimingUseCases.observeTyreWearLaps(), pitTimingUseCases.observeFuelLaps()) { tyre, fuel ->
                tyre to fuel
            },
            pitTimingUseCases.observeEnabledStates(),
            combine(
                combine(readout.observeText(), readout.observeImminentText()) { text, imminent -> text to imminent },
                combine(readout.observeTyreWearText(), readout.observeTyreWearImminentText()) { text, imminent ->
                    text to imminent
                },
                combine(readout.observeFuelText(), readout.observeFuelImminentText()) { text, imminent ->
                    text to imminent
                },
            ) { virtualEnergy, tyreWear, fuel -> Triple(virtualEnergy, tyreWear, fuel) },
            preview.textToSpeechAvailable,
        ) { virtualEnergyLaps, tyreWearLaps, enabledStates, texts, available ->
            LmuWindowsAutoReadoutPitTimingDetailUiState(
                virtualEnergyEnabled = enabledStates.getValue(LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy),
                virtualEnergyLaps = virtualEnergyLaps,
                virtualEnergyText = texts.first.first,
                virtualEnergyImminentText = texts.first.second,
                isTextToSpeechAvailable = available,
                tyreWearEnabled = enabledStates.getValue(LmuWindowsReadoutItemKey.PitTiming.TyreWear),
                tyreWearLaps = tyreWearLaps.first,
                fuelEnabled = enabledStates.getValue(LmuWindowsReadoutItemKey.PitTiming.Fuel),
                fuelLaps = tyreWearLaps.second,
                fuelText = texts.third.first,
                fuelImminentText = texts.third.second,
                tyreWearText = texts.second.first,
                tyreWearImminentText = texts.second.second,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsAutoReadoutPitTimingDetailUiState(),
        )

    /** ペインを離れるときに試聴を止める。 */
    fun onPreviewStopped() = preview.stop()

    fun onVirtualEnergyEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            pitTimingUseCases.saveEnabledState(LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy, enabled)
        }
    }

    fun onVirtualEnergyLapsChanged(laps: Int) {
        viewModelScope.launch {
            pitTimingUseCases.saveVirtualEnergyLaps(laps)
        }
    }

    fun onTyreWearEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            pitTimingUseCases.saveEnabledState(LmuWindowsReadoutItemKey.PitTiming.TyreWear, enabled)
        }
    }

    fun onFuelEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            pitTimingUseCases.saveEnabledState(LmuWindowsReadoutItemKey.PitTiming.Fuel, enabled)
        }
    }

    fun onTyreWearLapsChanged(laps: Int) {
        viewModelScope.launch {
            pitTimingUseCases.saveTyreWearLaps(laps)
        }
    }

    fun onFuelLapsChanged(laps: Int) {
        viewModelScope.launch {
            pitTimingUseCases.saveFuelLaps(laps)
        }
    }

    fun onVirtualEnergyTextChanged(text: String) {
        viewModelScope.launch { readout.saveText(text) }
    }

    fun onVirtualEnergyImminentTextChanged(text: String) {
        viewModelScope.launch { readout.saveImminentText(text) }
    }

    fun onVirtualEnergyTextPreviewClicked(
        text: String,
        laps: Int,
    ) {
        playReadoutPreview(formatLmuWindowsPitTimingReadoutText(text, laps))
    }

    fun onVirtualEnergyImminentTextPreviewClicked(text: String) {
        playReadoutPreview(text)
    }

    fun onTyreWearTextChanged(text: String) {
        viewModelScope.launch { readout.saveTyreWearText(text) }
    }

    fun onFuelTextChanged(text: String) {
        viewModelScope.launch { readout.saveFuelText(text) }
    }

    fun onTyreWearImminentTextChanged(text: String) {
        viewModelScope.launch { readout.saveTyreWearImminentText(text) }
    }

    fun onFuelImminentTextChanged(text: String) {
        viewModelScope.launch { readout.saveFuelImminentText(text) }
    }

    fun onTyreWearTextPreviewClicked(
        text: String,
        laps: Int,
    ) {
        playReadoutPreview(formatLmuWindowsPitTimingReadoutText(text, laps))
    }

    fun onFuelTextPreviewClicked(
        text: String,
        laps: Int,
    ) {
        playReadoutPreview(formatLmuWindowsPitTimingReadoutText(text, laps))
    }

    fun onTyreWearImminentTextPreviewClicked(text: String) {
        playReadoutPreview(text)
    }

    fun onFuelImminentTextPreviewClicked(text: String) {
        playReadoutPreview(text)
    }

    /** 空白文言・TTS利用不可・音量ゼロでは開始音も本文も再生しない。 */
    private fun playReadoutPreview(text: String) {
        preview.onPreviewClicked(text, LmuWindowsReadoutItemKey.PitTiming.Root)
    }
}
