package kurou.kodriver.feature.lmuwindowsautoreadout.remainingvirtualenergydetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.readoutEnabled

/** UI 状態の合成だけを担う cold Flow。共有・初期値は ViewModel が管理する。 */
internal class LmuWindowsAutoReadoutRemainingVirtualEnergyDetailFlow(
    thresholdPercentages: Flow<Int>,
    enabledStates: Flow<Map<ReadoutItemKey, Boolean>>,
    readoutTexts: Flow<String>,
    isTextToSpeechAvailable: Flow<Boolean>,
) {
    val uiState: Flow<LmuWindowsAutoReadoutRemainingVirtualEnergyDetailUiState> =
        combine(
            thresholdPercentages,
            enabledStates,
            readoutTexts,
            isTextToSpeechAvailable,
        ) { thresholdPercentage, states, text, isAvailable ->
            LmuWindowsAutoReadoutRemainingVirtualEnergyDetailUiState(
                thresholdPercentage = thresholdPercentage,
                readoutText = text,
                isTextToSpeechAvailable = isAvailable,
                enabled = states.readoutEnabled(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.WarningReadout),
            )
        }
}
