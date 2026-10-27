package kurou.kodriver.feature.gt7ps5autoreadout.remainingfuellapsdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.readoutEnabled

/** UI 状態の合成だけを担う cold Flow。共有・初期値は ViewModel が管理する。 */
internal class Gt7Ps5AutoReadoutRemainingFuelLapsDetailFlow(
    remainingFuelLaps: Flow<Int>,
    enabledStates: Flow<Map<ReadoutItemKey, Boolean>>,
    readoutText: Flow<String>,
    emptyReadoutText: Flow<String>,
    isTextToSpeechAvailable: Flow<Boolean>,
) {
    val uiState: Flow<Gt7Ps5AutoReadoutRemainingFuelLapsDetailUiState> =
        combine(
            remainingFuelLaps,
            enabledStates,
            readoutText,
            emptyReadoutText,
            isTextToSpeechAvailable,
        ) { laps, states, text, emptyText, available ->
            Gt7Ps5AutoReadoutRemainingFuelLapsDetailUiState(
                remainingFuelLaps = laps,
                enabled = states.readoutEnabled(Gt7Ps5ReadoutItemKey.RemainingFuelLaps.DetailEnabled),
                readoutText = text,
                emptyReadoutText = emptyText,
                isTextToSpeechAvailable = available,
            )
        }
}
