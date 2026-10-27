package kurou.kodriver.feature.lmuwindowsautoreadout.flagdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.ReadoutItemKey

/** UI 状態の合成だけを担う cold Flow。共有・初期値は ViewModel が管理する。 */
internal class LmuWindowsAutoReadoutFlagDetailFlow(
    enabledStates: Flow<Map<ReadoutItemKey, Boolean>>,
    flagTexts: Flow<Map<FlagReadoutItem, String>>,
    isTextToSpeechAvailable: Flow<Boolean>,
) {
    val uiState: Flow<LmuWindowsAutoReadoutFlagDetailUiState> =
        combine(enabledStates, flagTexts, isTextToSpeechAvailable) { enabled, texts, isAvailable ->
            LmuWindowsAutoReadoutFlagDetailUiState(
                enabledStates = enabled,
                flagTexts = texts,
                isTextToSpeechAvailable = isAvailable,
            )
        }
}
