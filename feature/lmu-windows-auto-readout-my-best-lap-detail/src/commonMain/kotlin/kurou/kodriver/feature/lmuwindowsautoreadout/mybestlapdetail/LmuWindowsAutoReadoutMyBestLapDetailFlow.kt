package kurou.kodriver.feature.lmuwindowsautoreadout.mybestlapdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.readoutEnabled

/** UI 状態の合成だけを担う cold Flow。共有・初期値は ViewModel が管理する。 */
internal class LmuWindowsAutoReadoutMyBestLapDetailFlow(
    enabledStates: Flow<Map<ReadoutItemKey, Boolean>>,
    readoutText: Flow<String>,
    isTextToSpeechAvailable: Flow<Boolean>,
) {
    val uiState: Flow<LmuWindowsAutoReadoutMyBestLapDetailUiState> =
        combine(enabledStates, readoutText, isTextToSpeechAvailable) { states, text, available ->
            LmuWindowsAutoReadoutMyBestLapDetailUiState(
                enabled = states.readoutEnabled(LmuWindowsReadoutItemKey.MyBestLap.DetailEnabled),
                readoutText = text,
                isTextToSpeechAvailable = available,
            )
        }
}
