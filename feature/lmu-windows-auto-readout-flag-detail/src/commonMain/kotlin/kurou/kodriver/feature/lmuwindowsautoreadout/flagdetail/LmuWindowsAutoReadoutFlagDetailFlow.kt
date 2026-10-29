package kurou.kodriver.feature.lmuwindowsautoreadout.flagdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.ReadoutItemKey

/** フラッグ文言と UI 状態を合成する cold Flow。共有・初期値は ViewModel が管理する。 */
internal class LmuWindowsAutoReadoutFlagDetailFlow(
    enabledStates: Flow<Map<ReadoutItemKey, Boolean>>,
    flagTextFlows: Map<FlagReadoutItem, Flow<String>>,
    isTextToSpeechAvailable: Flow<Boolean>,
) {
    private val flagTexts: Flow<Map<FlagReadoutItem, String>> =
        combine(flagTextFlows.map { (item, texts) -> texts.map { item to it } }) { it.toMap() }

    val uiState: Flow<LmuWindowsAutoReadoutFlagDetailUiState> =
        combine(enabledStates, flagTexts, isTextToSpeechAvailable) { enabled, texts, isAvailable ->
            LmuWindowsAutoReadoutFlagDetailUiState(
                enabledStates = enabled,
                flagTexts = texts,
                isTextToSpeechAvailable = isAvailable,
            )
        }
}
