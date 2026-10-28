package kurou.kodriver.feature.otherfeedbackdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.TelemetryLog

/** フォーム状態、添付ログ、送信可否から画面状態を合成する冷たい Flow。 */
internal class OtherFeedbackDetailFlow(
    mutableState: Flow<OtherFeedbackDetailUiState>,
    attachedTelemetryLog: Flow<TelemetryLog?>,
    canSendFeedback: Flow<Boolean>,
) {
    val initialUiState = OtherFeedbackDetailUiState()

    val uiState: Flow<OtherFeedbackDetailUiState> =
        combine(mutableState, attachedTelemetryLog, canSendFeedback) { state, log, canSend ->
            state.copy(attachedTelemetryLog = log, isCoolingDown = !canSend)
        }
}
