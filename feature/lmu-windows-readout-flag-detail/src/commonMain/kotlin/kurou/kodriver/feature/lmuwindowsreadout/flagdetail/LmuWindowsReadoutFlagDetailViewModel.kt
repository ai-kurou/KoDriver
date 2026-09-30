package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.RedFlagVoiceType
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsFlagEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsRedFlagVoiceTypeUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

internal data class FlagSettingsUseCases(
    val observeFlagEnabledStates: ObserveLmuWindowsFlagEnabledStatesUseCase,
    val observeRedFlagVoiceType: ObserveLmuWindowsRedFlagVoiceTypeUseCase,
    val saveFlagEnabledState: SaveLmuWindowsFlagEnabledStateUseCase,
    val saveRedFlagVoiceType: SaveLmuWindowsRedFlagVoiceTypeUseCase,
    val readoutTexts: FlagReadoutTextUseCases,
)

/** フラッグごとのカスタム読み上げ文言の Observe / Save UseCase を [FlagReadoutItem] で引けるようにまとめたもの。 */
internal data class FlagReadoutTextUseCases(
    val observeSectorYellowFlag: ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase,
    val observeBlueFlag: ObserveLmuWindowsBlueFlagReadoutTextUseCase,
    val observeFullCourseYellow: ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase,
    val observeRedFlag: ObserveLmuWindowsRedFlagReadoutTextUseCase,
    val saveSectorYellowFlag: SaveLmuWindowsSectorYellowFlagReadoutTextUseCase,
    val saveBlueFlag: SaveLmuWindowsBlueFlagReadoutTextUseCase,
    val saveFullCourseYellow: SaveLmuWindowsFullCourseYellowFlagReadoutTextUseCase,
    val saveRedFlag: SaveLmuWindowsRedFlagReadoutTextUseCase,
) {
    fun observe(item: FlagReadoutItem): Flow<String> =
        when (item) {
            FlagReadoutItem.BlueFlag -> observeBlueFlag()
            FlagReadoutItem.SectorYellowFlag -> observeSectorYellowFlag()
            FlagReadoutItem.FullCourseYellow -> observeFullCourseYellow()
        }

    suspend fun save(
        item: FlagReadoutItem,
        text: String,
    ) {
        when (item) {
            FlagReadoutItem.BlueFlag -> saveBlueFlag(text)
            FlagReadoutItem.SectorYellowFlag -> saveSectorYellowFlag(text)
            FlagReadoutItem.FullCourseYellow -> saveFullCourseYellow(text)
        }
    }
}

internal class LmuWindowsReadoutFlagDetailViewModel(
    private val settingsUseCases: FlagSettingsUseCases,
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
            settingsUseCases.readoutTexts.observeRedFlag(),
            combine(
                FlagReadoutItem.entries.map { item ->
                    settingsUseCases.readoutTexts.observe(item).map { item to it }
                },
            ) {
                it.toMap()
            },
            textToSpeechAvailable,
        ) { enabledStates, redFlagVoiceType, redFlagText, flagTexts, isTextToSpeechAvailable ->
            LmuWindowsReadoutFlagDetailUiState(
                enabledStates = enabledStates,
                redFlagVoiceType = redFlagVoiceType,
                redFlagText = redFlagText,
                flagTexts = flagTexts,
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

    fun onRedFlagTextChanged(text: String) {
        viewModelScope.launch { settingsUseCases.readoutTexts.saveRedFlag(text) }
    }

    /**
     * レッドフラッグのカスタム文言の試聴。文言が空のときは、実際の読み上げと同じく
     * 選択中の音声種別 [voiceType] の収録済みWAVを再生する。
     */
    fun onRedFlagTextPreviewClicked(
        text: String,
        voiceType: RedFlagVoiceType,
    ) {
        if (text.isBlank()) {
            onRedFlagPreviewClicked(voiceType)
        } else {
            viewModelScope.launch {
                playStartSoundForKey(ReadoutItemKey.LmuWindows.Flag.RedFlag)
                speakText(text)
            }
        }
    }

    fun onFlagTextChanged(
        item: FlagReadoutItem,
        text: String,
    ) {
        viewModelScope.launch { settingsUseCases.readoutTexts.save(item, text) }
    }

    /**
     * カスタム文言の試聴。文言が空のときは、実際の読み上げと同じく収録済みWAVを再生する。
     */
    fun onFlagTextPreviewClicked(
        item: FlagReadoutItem,
        text: String,
    ) {
        if (text.isBlank()) {
            playSpeechEvent(item.previewEvent)
        } else {
            viewModelScope.launch {
                playStartSoundForKey(item.key)
                speakText(text)
            }
        }
    }
}
