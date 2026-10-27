package kurou.kodriver.feature.gt7ps5autoreadout.remainingfueldetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.readoutEnabled

/** UI 状態の合成だけを担う cold Flow。共有・初期値は ViewModel が管理する。 */
internal class Gt7Ps5AutoReadoutRemainingFuelDetailFlow(
    thresholdPercentage: Flow<Int>,
    enabledStates: Flow<Map<ReadoutItemKey, Boolean>>,
    readoutText: Flow<String>,
    isTextToSpeechAvailable: Flow<Boolean>,
) {
    val uiState: Flow<Gt7Ps5AutoReadoutRemainingFuelDetailUiState> =
        combine(
            thresholdPercentage,
            enabledStates,
            readoutText,
            isTextToSpeechAvailable,
        ) { thresholdPercentage, enabledStates, text, available ->
            Gt7Ps5AutoReadoutRemainingFuelDetailUiState(
                thresholdPercentage = thresholdPercentage,
                readoutText = text,
                isTextToSpeechAvailable = available,
                enabled = enabledStates.readoutEnabled(Gt7Ps5ReadoutItemKey.RemainingFuel.DetailEnabled),
            )
        }
}
