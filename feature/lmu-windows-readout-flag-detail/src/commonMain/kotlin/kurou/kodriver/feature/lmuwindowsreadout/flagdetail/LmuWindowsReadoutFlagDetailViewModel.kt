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
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsFlagEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsRedFlagVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

@Suppress("LongParameterList")
internal class LmuWindowsReadoutFlagDetailViewModel(
    observeFlagEnabledStates: ObserveLmuWindowsFlagEnabledStatesUseCase,
    observeRedFlagVoiceType: ObserveLmuWindowsRedFlagVoiceTypeUseCase,
    observeSectorYellowFlagReadoutText: ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase,
    private val saveFlagEnabledState: SaveLmuWindowsFlagEnabledStateUseCase,
    private val saveRedFlagVoiceType: SaveLmuWindowsRedFlagVoiceTypeUseCase,
    private val saveSectorYellowFlagReadoutText: SaveLmuWindowsSectorYellowFlagReadoutTextUseCase,
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
            observeFlagEnabledStates(),
            observeRedFlagVoiceType(),
            observeSectorYellowFlagReadoutText(),
            textToSpeechAvailable,
        ) { enabledStates, redFlagVoiceType, sectorYellowFlagText, isTextToSpeechAvailable ->
            LmuWindowsReadoutFlagDetailUiState(
                enabledStates = enabledStates,
                redFlagVoiceType = redFlagVoiceType,
                sectorYellowFlagText = sectorYellowFlagText,
                isTextToSpeechAvailable = isTextToSpeechAvailable,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LmuWindowsReadoutFlagDetailUiState())

    fun onFlagEnabledChanged(
        item: FlagReadoutItem,
        enabled: Boolean,
    ) {
        viewModelScope.launch { saveFlagEnabledState(item.key, enabled) }
    }

    fun onRedFlagEnabledChanged(enabled: Boolean) {
        viewModelScope.launch { saveFlagEnabledState(ReadoutItemKey.LmuWindows.Flag.RedFlag, enabled) }
    }

    fun onPreviewClicked(item: FlagReadoutItem) {
        playSpeechEvent(item.previewEvent)
    }

    fun onRedFlagVoiceTypeChanged(type: RedFlagVoiceType) {
        viewModelScope.launch { saveRedFlagVoiceType(type) }
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
        viewModelScope.launch { saveSectorYellowFlagReadoutText(text) }
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
}
