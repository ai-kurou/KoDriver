package kurou.kodriver.feature.otherfeedbackdetail

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.FeedbackType
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.NarrationSource
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kotlin.test.Test
import kotlin.test.assertEquals

class OtherFeedbackDetailFlowTest {
    private val mutableState = MutableStateFlow(OtherFeedbackDetailUiState())
    private val attachedTelemetryLog = MutableStateFlow<TelemetryLog?>(null)
    private val canSendFeedback = MutableStateFlow(true)
    private val log =
        TelemetryLog(
            id = 1L,
            createdAt = 0L,
            simulator = Simulator.LmuWindows,
            readoutItemKey = LmuWindowsReadoutItemKey.Flag.Root,
            narratedText = "イエローフラッグ",
            narrationOutcome = NarrationOutcome.INTERRUPTED,
            narrationSource = NarrationSource.AUTO,
            telemetryJson = "{}",
        )

    private fun createFlow() =
        OtherFeedbackDetailFlow(
            mutableState = mutableState,
            attachedTelemetryLog = attachedTelemetryLog,
            canSendFeedback = canSendFeedback,
        )

    @Test
    fun `初期状態と添付ログなしの画面状態を返す`() =
        runTest {
            val flow = createFlow()
            assertEquals(OtherFeedbackDetailUiState(), flow.initialUiState)
            assertEquals(OtherFeedbackDetailUiState(), flow.uiState.first())
        }

    @Test
    fun `添付ログと送信可否の変化を反映しフォーム状態を保持する`() =
        runTest {
            val flow = createFlow()
            val statuses =
                listOf(
                    FeedbackSendStatus.Idle,
                    FeedbackSendStatus.Sending,
                    FeedbackSendStatus.Sent,
                    FeedbackSendStatus.Failed,
                )
            for (status in statuses) {
                val form =
                    OtherFeedbackDetailUiState(
                        type = FeedbackType.FeatureRequest,
                        message = "本文",
                        name = "Kurou",
                        email = "user@example.com",
                        sendStatus = status,
                        showMessageError = true,
                        showNameError = true,
                        showEmailError = true,
                        attachedTelemetryLog = log,
                        isCoolingDown = true,
                    )
                mutableState.update { form }
                for (attachment in listOf(null, log, log.copy(id = 2L), null)) {
                    attachedTelemetryLog.update { attachment }
                    for (canSend in listOf(true, false, true)) {
                        canSendFeedback.update { canSend }
                        assertEquals(
                            form.copy(attachedTelemetryLog = attachment, isCoolingDown = !canSend),
                            flow.uiState.first(),
                        )
                    }
                }
            }
            mutableState.update { OtherFeedbackDetailUiState() }
            assertEquals(OtherFeedbackDetailUiState(), flow.uiState.first())
        }
}
