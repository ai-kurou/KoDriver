package kurou.kodriver.feature.otherfeedbackdetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.usecase.ObserveTelemetryLogDetailUseCase

/** 添付ログIDからログを取得し、フォーム状態・送信可否と合わせて画面状態を合成する冷たい Flow。 */
internal class OtherFeedbackDetailFlow(
    mutableState: Flow<OtherFeedbackDetailUiState>,
    telemetryLogId: Flow<Long?>,
    observeTelemetryLogDetail: ObserveTelemetryLogDetailUseCase,
    canSendFeedback: Flow<Boolean>,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val attachedTelemetryLog =
        telemetryLogId.flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                observeTelemetryLogDetail(id).map { it?.current }
            }
        }

    val uiState: Flow<OtherFeedbackDetailUiState> =
        combine(mutableState, attachedTelemetryLog, canSendFeedback) { state, log, canSend ->
            state.copy(attachedTelemetryLog = log, isCoolingDown = !canSend)
        }
}
