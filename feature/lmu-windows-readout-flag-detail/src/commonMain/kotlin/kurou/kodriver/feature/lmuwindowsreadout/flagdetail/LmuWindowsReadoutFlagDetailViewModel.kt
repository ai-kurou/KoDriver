package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.RedFlagVoiceType
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFlagRecordedVoiceSelectedUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsFlagEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveLmuWindowsFlagRecordedVoiceSelectedUseCase
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

/** フラッグごとのカスタム読み上げ文言と収録音声の選択状態の Observe / Save UseCase を [FlagReadoutItem] で引けるようにまとめたもの。 */
internal data class FlagReadoutTextUseCases(
    val observeSectorYellowFlag: ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase,
    val observeBlueFlag: ObserveLmuWindowsBlueFlagReadoutTextUseCase,
    val observeFullCourseYellow: ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase,
    val observeRedFlag: ObserveLmuWindowsRedFlagReadoutTextUseCase,
    val saveSectorYellowFlag: SaveLmuWindowsSectorYellowFlagReadoutTextUseCase,
    val saveBlueFlag: SaveLmuWindowsBlueFlagReadoutTextUseCase,
    val saveFullCourseYellow: SaveLmuWindowsFullCourseYellowFlagReadoutTextUseCase,
    val saveRedFlag: SaveLmuWindowsRedFlagReadoutTextUseCase,
    val observeRecordedVoiceSelected: ObserveLmuWindowsFlagRecordedVoiceSelectedUseCase,
    val saveRecordedVoiceSelected: SaveLmuWindowsFlagRecordedVoiceSelectedUseCase,
) {
    fun observeRecordedVoiceSelected(item: FlagReadoutItem): Flow<Boolean> = observeRecordedVoiceSelected(item.target)

    suspend fun saveRecordedVoiceSelected(
        item: FlagReadoutItem,
        selected: Boolean,
    ) {
        saveRecordedVoiceSelected(item.target, selected)
    }

    fun observe(item: FlagReadoutItem): Flow<String> =
        when (item) {
            FlagReadoutItem.BlueFlag -> observeBlueFlag()
            FlagReadoutItem.SectorYellowFlag -> observeSectorYellowFlag()
            FlagReadoutItem.FullCourseYellow -> observeFullCourseYellow()
            FlagReadoutItem.RedFlag -> observeRedFlag()
        }

    suspend fun save(
        item: FlagReadoutItem,
        text: String,
    ) {
        when (item) {
            FlagReadoutItem.BlueFlag -> saveBlueFlag(text)
            FlagReadoutItem.SectorYellowFlag -> saveSectorYellowFlag(text)
            FlagReadoutItem.FullCourseYellow -> saveFullCourseYellow(text)
            FlagReadoutItem.RedFlag -> saveRedFlag(text)
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
            combine(
                FlagReadoutItem.entries.map { item ->
                    settingsUseCases.readoutTexts.observe(item).map { item to it }
                },
            ) {
                it.toMap()
            },
            combine(
                FlagReadoutItem.entries.map { item ->
                    settingsUseCases.readoutTexts.observeRecordedVoiceSelected(item).map { item to it }
                },
            ) {
                it.toMap()
            },
            textToSpeechAvailable,
        ) { enabledStates, redFlagVoiceType, flagTexts, recordedVoiceSelected, isTextToSpeechAvailable ->
            LmuWindowsReadoutFlagDetailUiState(
                enabledStates = enabledStates,
                redFlagVoiceType = redFlagVoiceType,
                flagTexts = flagTexts,
                recordedVoiceSelected = recordedVoiceSelected,
                isTextToSpeechAvailable = isTextToSpeechAvailable,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LmuWindowsReadoutFlagDetailUiState())

    fun onFlagEnabledChanged(
        item: FlagReadoutItem,
        enabled: Boolean,
    ) {
        viewModelScope.launch { settingsUseCases.saveFlagEnabledState(item.key, enabled) }
    }

    fun onPreviewClicked(item: FlagReadoutItem) {
        playSpeechEvent(item.previewEvent)
    }

    fun onRedFlagVoiceTypeChanged(type: RedFlagVoiceType) {
        viewModelScope.launch { settingsUseCases.saveRedFlagVoiceType(type) }
    }

    fun onRedFlagPreviewClicked(type: RedFlagVoiceType) {
        playSpeechEvent(type.speechEvent())
    }

    fun onFlagTextChanged(
        item: FlagReadoutItem,
        text: String,
    ) {
        viewModelScope.launch {
            settingsUseCases.readoutTexts.save(item, text)
            // 文言を入力した時点でカスタム文言を使う意思とみなし、収録音声の明示選択を解除する。
            if (text.isNotEmpty()) settingsUseCases.readoutTexts.saveRecordedVoiceSelected(item, false)
        }
    }

    /**
     * 収録音声のチップが選ばれたとき。入力済みのカスタム文言は残したまま、収録音声で読み上げる設定にする。
     */
    fun onRecordedVoiceSelected(item: FlagReadoutItem) {
        viewModelScope.launch { settingsUseCases.readoutTexts.saveRecordedVoiceSelected(item, true) }
    }

    /**
     * カスタム文言の試聴。文言が空のときは、実際の読み上げと同じく収録済みWAVを再生する。
     * レッドフラッグは音声種別（RedFlag / SessionStop）で収録音声が異なるため、選択中の種別のWAVを再生する。
     */
    fun onFlagTextPreviewClicked(
        item: FlagReadoutItem,
        text: String,
    ) {
        viewModelScope.launch {
            if (text.isBlank()) {
                playSpeechEvent(previewEvent(item))
            } else {
                playStartSoundForKey(item.key)
                speakText(text)
            }
        }
    }

    private suspend fun previewEvent(item: FlagReadoutItem): SpeechEvent =
        if (item == FlagReadoutItem.RedFlag) {
            settingsUseCases.observeRedFlagVoiceType().first().speechEvent()
        } else {
            item.previewEvent
        }

    private fun RedFlagVoiceType.speechEvent(): SpeechEvent =
        when (this) {
            RedFlagVoiceType.RED_FLAG -> SpeechEvent.RedFlag
            RedFlagVoiceType.SESSION_STOP -> SpeechEvent.SessionStop
        }
}
