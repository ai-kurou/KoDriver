package kurou.kodriver.feature.acewindowsautoreadout.remainingfuellapsdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.readoutEnabled

/** UI 状態の合成だけを担う cold Flow。共有・初期値は ViewModel が管理する。 */
internal class AceWindowsAutoReadoutRemainingFuelLapsDetailFlow(
    remainingFuelLaps: Flow<Int>,
    enabledStates: Flow<Map<ReadoutItemKey, Boolean>>,
    readoutText: Flow<String>,
    emptyReadoutText: Flow<String>,
    isTextToSpeechAvailable: Flow<Boolean>,
) {
    val uiState: Flow<AceWindowsAutoReadoutRemainingFuelLapsDetailUiState> =
        combine(
            remainingFuelLaps,
            enabledStates,
            readoutText,
            emptyReadoutText,
            isTextToSpeechAvailable,
        ) { laps, states, text, emptyText, available ->
            AceWindowsAutoReadoutRemainingFuelLapsDetailUiState(
                remainingFuelLaps = laps,
                enabled = states.readoutEnabled(AceWindowsReadoutItemKey.RemainingFuelLaps.DetailEnabled),
                readoutText = text,
                emptyReadoutText = emptyText,
                isTextToSpeechAvailable = available,
            )
        }
}
