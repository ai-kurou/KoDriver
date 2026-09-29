package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.RedFlagVoiceType
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBlackFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsBlackFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsFlagEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsRedFlagVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

internal data class FlagSettingsUseCases(
    val observeFlagEnabledStates: ObserveLmuWindowsFlagEnabledStatesUseCase,
    val observeRedFlagVoiceType: ObserveLmuWindowsRedFlagVoiceTypeUseCase,
    val observeSectorYellowFlagReadoutText: ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase,
    val saveFlagEnabledState: SaveLmuWindowsFlagEnabledStateUseCase,
    val saveRedFlagVoiceType: SaveLmuWindowsRedFlagVoiceTypeUseCase,
    val saveSectorYellowFlagReadoutText: SaveLmuWindowsSectorYellowFlagReadoutTextUseCase,
)

internal data class BlackFlagReadoutTextUseCases(
    val observe: ObserveLmuWindowsBlackFlagReadoutTextUseCase,
    val save: SaveLmuWindowsBlackFlagReadoutTextUseCase,
)

internal class LmuWindowsReadoutFlagDetailViewModel(
    private val settingsUseCases: FlagSettingsUseCases,
    private val blackFlagReadoutTextUseCases: BlackFlagReadoutTextUseCases,
    private val playSpeechEvent: PlaySpeechEventUseCase,
    private val speakText: SpeakTextUseCase,
    private val playStartSoundForKey: PlayStartSoundForKeyUseCase,
    checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
) : ViewModel() {
    private val textToSpeechAvailable = MutableStateFlow(false)

    init {
        viewModelScope.launch { textToSpeechAvailable.value = checkTextToSpeechAvailable() }
    }

    val uiState: StateFlow<LmuWindowsReadoutFlagDetailUiState> =
        combine(
            settingsUseCases.observeFlagEnabledStates(),
            settingsUseCases.observeRedFlagVoiceType(),
            settingsUseCases.observeSectorYellowFlagReadoutText(),
            blackFlagReadoutTextUseCases.observe(),
            textToSpeechAvailable,
        ) { enabledStates, redFlagVoiceType, sectorYellowFlagText, blackFlagText, isTextToSpeechAvailable ->
            LmuWindowsReadoutFlagDetailUiState(
                enabledStates = enabledStates,
                redFlagVoiceType = redFlagVoiceType,
                sectorYellowFlagText = sectorYellowFlagText,
                blackFlagText = blackFlagText,
                isTextToSpeechAvailable = isTextToSpeechAvailable,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LmuWindowsReadoutFlagDetailUiState())

    fun onFlagEnabledChanged(
        item: FlagReadoutItem,
        enabled: Boolean,
    ) {
        viewModelScope.launch { settingsUseCases.saveFlagEnabledState(item.key, enabled) }
    }

    fun onRedFlagEnabledChanged(enabled: Boolean) {
        viewModelScope.launch { settingsUseCases.saveFlagEnabledState(ReadoutItemKey.LmuWindows.Flag.RedFlag, enabled) }
    }

    fun onPreviewClicked(item: FlagReadoutItem) {
        playSpeechEvent(item.previewEvent)
    }

    fun onRedFlagVoiceTypeChanged(type: RedFlagVoiceType) {
        viewModelScope.launch { settingsUseCases.saveRedFlagVoiceType(type) }
    }

    fun onRedFlagPreviewClicked(type: RedFlagVoiceType) {
        playSpeechEvent(
            when (type) {
                RedFlagVoiceType.RED_FLAG -> SpeechEvent.RedFlag
                RedFlagVoiceType.SESSION_STOP -> SpeechEvent.SessionStop
            },
        )
    }

    fun onSectorYellowFlagTextChanged(text: String) {
        viewModelScope.launch { settingsUseCases.saveSectorYellowFlagReadoutText(text) }
    }

    /**
     * カスタム文言の試聴。文言が空のときは、実際の読み上げと同じく収録済みWAVを再生する。
     */
    fun onSectorYellowFlagTextPreviewClicked(text: String) {
        if (text.isBlank()) {
            playSpeechEvent(SpeechEvent.YellowFlag)
        } else {
            viewModelScope.launch {
                playStartSoundForKey(ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag)
                speakText(text)
            }
        }
    }

    fun onBlackFlagTextChanged(text: String) {
        viewModelScope.launch { blackFlagReadoutTextUseCases.save(text) }
    }

    /** Clear the custom text and preview the black flag chip with text-to-speech. */
    fun onBlackFlagChipClicked(text: String) {
        viewModelScope.launch {
            blackFlagReadoutTextUseCases.save("")
            playStartSoundForKey(ReadoutItemKey.LmuWindows.Flag.Root)
            speakText(text)
        }
    }

    /** Black flag telemetry is not currently exposed by LMU shared memory; this previews only the saved custom text. */
    fun onBlackFlagTextPreviewClicked(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            playStartSoundForKey(ReadoutItemKey.LmuWindows.Flag.Root)
            speakText(text)
        }
    }
}
