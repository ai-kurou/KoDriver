package kurou.kodriver.feature.lmuwindowsautoreadout.flagdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsFlagEnabledStateUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.preview.ReadoutTextPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

internal data class FlagSettingsUseCases(
    val observeFlagEnabledStates: ObserveLmuWindowsFlagEnabledStatesUseCase,
    val saveFlagEnabledState: SaveLmuWindowsFlagEnabledStateUseCase,
    val readoutTexts: FlagReadoutTextUseCases,
)

/** フラッグごとの読み上げ文言の Observe / Save UseCase を [FlagReadoutItem] で引けるようにまとめたもの。 */
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

internal class LmuWindowsAutoReadoutFlagDetailViewModel(
    private val settingsUseCases: FlagSettingsUseCases,
    speakText: SpeakTextUseCase,
    playStartSoundForKey: PlayStartSoundForKeyUseCase,
    checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    observeSoundVolume: ObserveSoundVolumeUseCase,
) : ViewModel() {
    private val preview =
        ReadoutTextPreviewHelper(
            viewModelScope,
            checkTextToSpeechAvailable,
            observeSoundVolume,
            playStartSoundForKey,
            speakText,
        )

    private val flagTexts: Flow<Map<FlagReadoutItem, String>> =
        combine(
            FlagReadoutItem.entries.map { item ->
                settingsUseCases.readoutTexts.observe(item).map { item to it }
            },
        ) {
            it.toMap()
        }

    val uiState: StateFlow<LmuWindowsAutoReadoutFlagDetailUiState> =
        LmuWindowsAutoReadoutFlagDetailFlow(
            enabledStates = settingsUseCases.observeFlagEnabledStates(),
            flagTexts = flagTexts,
            isTextToSpeechAvailable = preview.textToSpeechAvailable,
        ).uiState.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsAutoReadoutFlagDetailUiState(),
        )

    /** ペインを離れるときに試聴を止める。 */
    fun onPreviewStopped() = preview.stop()

    fun onFlagEnabledChanged(
        item: FlagReadoutItem,
        enabled: Boolean,
    ) {
        viewModelScope.launch { settingsUseCases.saveFlagEnabledState(item.key, enabled) }
    }

    fun onFlagTextChanged(
        item: FlagReadoutItem,
        text: String,
    ) {
        viewModelScope.launch { settingsUseCases.readoutTexts.save(item, text) }
    }

    fun onFlagTextReset(item: FlagReadoutItem) {
        viewModelScope.launch { settingsUseCases.readoutTexts.save(item, item.defaultText) }
    }

    /**
     * 空白文言・TTS利用不可時は試聴しない。本文はOS標準TTSのみで読み上げる。
     * 開始音の有効設定は読み上げ一覧のトップレベル項目 [LmuWindowsReadoutItemKey.Flag.Root] に保存され、
     * 実際の読み上げもそのキーで判定するため、試聴でも個別フラッグのキーではなくそれを渡す。
     */
    fun onFlagTextPreviewClicked(text: String) {
        preview.onPreviewClicked(text, LmuWindowsReadoutItemKey.Flag.Root)
    }
}
