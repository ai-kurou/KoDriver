package kurou.kodriver.feature.acewindowsautoreadout.flagdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsFlagEnabledStateUseCase
import kurou.kodriver.domain.acewindows.usecase.SaveAceWindowsFlagReadoutTextUseCase
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.preview.ReadoutTextPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

internal data class FlagSettingsUseCases(
    val observeFlagEnabledStates: ObserveAceWindowsFlagEnabledStatesUseCase,
    val saveFlagEnabledState: SaveAceWindowsFlagEnabledStateUseCase,
    val readoutTexts: FlagReadoutTextUseCases,
)

/** フラッグごとの読み上げ文言の Observe / Save UseCase を [FlagReadoutItem] で引けるようにまとめたもの。 */
internal data class FlagReadoutTextUseCases(
    val observeFlagText: ObserveAceWindowsFlagReadoutTextUseCase,
    val saveFlagText: SaveAceWindowsFlagReadoutTextUseCase,
) {
    fun observe(item: FlagReadoutItem): Flow<String> = observeFlagText(item.textKey)

    suspend fun save(
        item: FlagReadoutItem,
        text: String,
    ) {
        saveFlagText(item.textKey, text)
    }
}

internal class AceWindowsAutoReadoutFlagDetailViewModel(
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

    val uiState: StateFlow<AceWindowsAutoReadoutFlagDetailUiState> =
        AceWindowsAutoReadoutFlagDetailFlow(
            enabledStates = settingsUseCases.observeFlagEnabledStates(),
            flagTextFlows = FlagReadoutItem.entries.associateWith { settingsUseCases.readoutTexts.observe(it) },
            isTextToSpeechAvailable = preview.textToSpeechAvailable,
        ).uiState.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AceWindowsAutoReadoutFlagDetailUiState(),
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
        val defaultText = item.defaultText
        viewModelScope.launch { settingsUseCases.readoutTexts.save(item, defaultText) }
    }

    /**
     * 空白文言・TTS利用不可時は試聴しない。本文はOS標準TTSのみで読み上げる。
     * 開始音の有効設定は読み上げ一覧のトップレベル項目 [AceWindowsReadoutItemKey.Flag.Root] に保存され、
     * 実際の読み上げもそのキーで判定するため、試聴でも個別フラッグのキーではなくそれを渡す。
     */
    fun onFlagTextPreviewClicked(text: String) {
        preview.onPreviewClicked(text, AceWindowsReadoutItemKey.Flag.Root)
    }
}
