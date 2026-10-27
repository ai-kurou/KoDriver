package kurou.kodriver.feature.gt7ps5autoreadout.mybestlapdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.readoutEnabled

/** UI 状態の合成だけを担う cold Flow。共有・初期値は ViewModel が管理する。 */
internal class Gt7Ps5AutoReadoutMyBestLapDetailFlow(
    enabledStates: Flow<Map<ReadoutItemKey, Boolean>>,
    readoutText: Flow<String>,
    isTextToSpeechAvailable: Flow<Boolean>,
) {
    val uiState: Flow<Gt7Ps5AutoReadoutMyBestLapDetailUiState> =
        combine(enabledStates, readoutText, isTextToSpeechAvailable) { states, text, available ->
            Gt7Ps5AutoReadoutMyBestLapDetailUiState(
                enabled = states.readoutEnabled(Gt7Ps5ReadoutItemKey.MyBestLap.DetailEnabled),
                readoutText = text,
                isTextToSpeechAvailable = available,
            )
        }
}
