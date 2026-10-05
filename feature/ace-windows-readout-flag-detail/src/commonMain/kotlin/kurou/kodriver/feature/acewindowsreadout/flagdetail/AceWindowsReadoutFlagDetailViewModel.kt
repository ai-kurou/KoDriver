package kurou.kodriver.feature.acewindowsreadout.flagdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.preview.ReadoutTextPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlackFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlackWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsCheckeredFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsGreenFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsOrangeCircleFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRedYellowStripesFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsBlackFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsBlackWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsCheckeredFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsFlagEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsGreenFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsOrangeCircleFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsRedYellowStripesFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveAceWindowsYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

internal data class FlagSettingsUseCases(
    val observeFlagEnabledStates: ObserveAceWindowsFlagEnabledStatesUseCase,
    val saveFlagEnabledState: SaveAceWindowsFlagEnabledStateUseCase,
    val readoutTexts: FlagReadoutTextUseCases,
)

/** フラッグごとの読み上げ文言の Observe / Save UseCase を [FlagReadoutItem] で引けるようにまとめたもの。 */
@Suppress("LongParameterList")
internal data class FlagReadoutTextUseCases(
    val observeCheckeredFlag: ObserveAceWindowsCheckeredFlagReadoutTextUseCase,
    val saveCheckeredFlag: SaveAceWindowsCheckeredFlagReadoutTextUseCase,
    val observeWhiteFlag: ObserveAceWindowsWhiteFlagReadoutTextUseCase,
    val saveWhiteFlag: SaveAceWindowsWhiteFlagReadoutTextUseCase,
    val observeGreenFlag: ObserveAceWindowsGreenFlagReadoutTextUseCase,
    val saveGreenFlag: SaveAceWindowsGreenFlagReadoutTextUseCase,
    val observeRedFlag: ObserveAceWindowsRedFlagReadoutTextUseCase,
    val saveRedFlag: SaveAceWindowsRedFlagReadoutTextUseCase,
    val observeBlueFlag: ObserveAceWindowsBlueFlagReadoutTextUseCase,
    val saveBlueFlag: SaveAceWindowsBlueFlagReadoutTextUseCase,
    val observeYellowFlag: ObserveAceWindowsYellowFlagReadoutTextUseCase,
    val saveYellowFlag: SaveAceWindowsYellowFlagReadoutTextUseCase,
    val observeBlackFlag: ObserveAceWindowsBlackFlagReadoutTextUseCase,
    val saveBlackFlag: SaveAceWindowsBlackFlagReadoutTextUseCase,
    val observeBlackWhiteFlag: ObserveAceWindowsBlackWhiteFlagReadoutTextUseCase,
    val saveBlackWhiteFlag: SaveAceWindowsBlackWhiteFlagReadoutTextUseCase,
    val observeOrangeCircleFlag: ObserveAceWindowsOrangeCircleFlagReadoutTextUseCase,
    val saveOrangeCircleFlag: SaveAceWindowsOrangeCircleFlagReadoutTextUseCase,
    val observeRedYellowStripesFlag: ObserveAceWindowsRedYellowStripesFlagReadoutTextUseCase,
    val saveRedYellowStripesFlag: SaveAceWindowsRedYellowStripesFlagReadoutTextUseCase,
) {
    fun observe(item: FlagReadoutItem): Flow<String> =
        when (item) {
            FlagReadoutItem.CheckeredFlag -> observeCheckeredFlag()
            FlagReadoutItem.WhiteFlag -> observeWhiteFlag()
            FlagReadoutItem.GreenFlag -> observeGreenFlag()
            FlagReadoutItem.RedFlag -> observeRedFlag()
            FlagReadoutItem.BlueFlag -> observeBlueFlag()
            FlagReadoutItem.YellowFlag -> observeYellowFlag()
            FlagReadoutItem.BlackFlag -> observeBlackFlag()
            FlagReadoutItem.BlackWhiteFlag -> observeBlackWhiteFlag()
            FlagReadoutItem.OrangeCircleFlag -> observeOrangeCircleFlag()
            FlagReadoutItem.RedYellowStripesFlag -> observeRedYellowStripesFlag()
        }

    suspend fun save(
        item: FlagReadoutItem,
        text: String,
    ) {
        when (item) {
            FlagReadoutItem.CheckeredFlag -> saveCheckeredFlag(text)
            FlagReadoutItem.WhiteFlag -> saveWhiteFlag(text)
            FlagReadoutItem.GreenFlag -> saveGreenFlag(text)
            FlagReadoutItem.RedFlag -> saveRedFlag(text)
            FlagReadoutItem.BlueFlag -> saveBlueFlag(text)
            FlagReadoutItem.YellowFlag -> saveYellowFlag(text)
            FlagReadoutItem.BlackFlag -> saveBlackFlag(text)
            FlagReadoutItem.BlackWhiteFlag -> saveBlackWhiteFlag(text)
            FlagReadoutItem.OrangeCircleFlag -> saveOrangeCircleFlag(text)
            FlagReadoutItem.RedYellowStripesFlag -> saveRedYellowStripesFlag(text)
        }
    }
}

internal class AceWindowsReadoutFlagDetailViewModel(
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

    val uiState: StateFlow<AceWindowsReadoutFlagDetailUiState> =
        combine(
            settingsUseCases.observeFlagEnabledStates(),
            combine(
                FlagReadoutItem.entries.map { item ->
                    settingsUseCases.readoutTexts.observe(item).map { item to it }
                },
            ) {
                it.toMap()
            },
            preview.textToSpeechAvailable,
        ) { enabledStates, flagTexts, isTextToSpeechAvailable ->
            AceWindowsReadoutFlagDetailUiState(
                enabledStates = enabledStates,
                flagTexts = flagTexts,
                isTextToSpeechAvailable = isTextToSpeechAvailable,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AceWindowsReadoutFlagDetailUiState())

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
     * 開始音の有効設定は読み上げ一覧のトップレベル項目 [ReadoutItemKey.AceWindows.Flag.Root] に保存され、
     * 実際の読み上げもそのキーで判定するため、試聴でも個別フラッグのキーではなくそれを渡す。
     */
    fun onFlagTextPreviewClicked(text: String) {
        viewModelScope.launch { preview.preview(text, ReadoutItemKey.AceWindows.Flag.Root) }
    }
}
