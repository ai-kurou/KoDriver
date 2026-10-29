package kurou.kodriver.feature.otherfeedbackdetail

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.FeedbackType
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.NarrationSource
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.model.TelemetryLogDetail
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.ObserveTelemetryLogDetailUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherFeedbackDetailFlowTest {
    private val mutableState = MutableStateFlow(OtherFeedbackDetailUiState())
    private val telemetryLogId = MutableStateFlow<Long?>(null)
    private val repository: TelemetryLogRepository = mockk()
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
            telemetryLogId = telemetryLogId,
            observeTelemetryLogDetail = ObserveTelemetryLogDetailUseCase(repository),
            canSendFeedback = canSendFeedback,
        )

    @Test
    fun `添付ログIDがnullならログを取得せず添付なしの画面状態を返す`() =
        runTest {
            val flow = createFlow()
            assertEquals(OtherFeedbackDetailUiState(), flow.uiState.first())
            confirmVerified(repository)
        }

    @Test
    fun `添付ログと送信可否の変化を反映しフォーム状態を保持する`() =
        runTest {
            every { repository.observeTelemetryLogDetail(1L) } returns flowOf(TelemetryLogDetail(log, null))
            every { repository.observeTelemetryLogDetail(2L) } returns
                flowOf(TelemetryLogDetail(log.copy(id = 2L), null))
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
                    telemetryLogId.update { attachment?.id }
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

    @Test
    fun `購読開始後にIDを切り替え詳細更新を反映し旧購読をキャンセルする`() =
        runTest {
            val firstDetail = MutableStateFlow<TelemetryLogDetail?>(TelemetryLogDetail(log, null))
            val secondLog = log.copy(id = 2L)
            val secondDetail = MutableStateFlow<TelemetryLogDetail?>(TelemetryLogDetail(secondLog, log))
            val completedIds = mutableListOf<Long>()
            every { repository.observeTelemetryLogDetail(1L) } returns
                firstDetail.onCompletion { completedIds.add(1L) }
            every { repository.observeTelemetryLogDetail(2L) } returns
                secondDetail.onCompletion { completedIds.add(2L) }
            telemetryLogId.update { 1L }
            val form = OtherFeedbackDetailUiState(message = "本文", sendStatus = FeedbackSendStatus.Sending)
            mutableState.update { form }
            val flow = createFlow()
            verify(exactly = 0) { repository.observeTelemetryLogDetail(1L) }
            verify(exactly = 0) { repository.observeTelemetryLogDetail(2L) }
            val states = mutableListOf<OtherFeedbackDetailUiState>()
            val job = backgroundScope.launch { flow.uiState.collect { states.add(it) } }
            runCurrent()
            assertEquals(listOf(form.copy(attachedTelemetryLog = log)), states)

            telemetryLogId.update { 2L }
            runCurrent()
            assertEquals(listOf(1L), completedIds)
            assertEquals(form.copy(attachedTelemetryLog = secondLog), states.last())

            val stateCount = states.size
            firstDetail.update { TelemetryLogDetail(log.copy(telemetryJson = "旧ログの更新"), null) }
            runCurrent()
            assertEquals(stateCount, states.size)

            val updatedLog = secondLog.copy(telemetryJson = "新ログの更新")
            secondDetail.update { TelemetryLogDetail(updatedLog, log) }
            canSendFeedback.update { false }
            runCurrent()
            assertEquals(form.copy(attachedTelemetryLog = updatedLog, isCoolingDown = true), states.last())

            secondDetail.update { null }
            runCurrent()
            assertEquals(form.copy(isCoolingDown = true), states.last())

            secondDetail.update { TelemetryLogDetail(updatedLog, log) }
            runCurrent()
            assertEquals(form.copy(attachedTelemetryLog = updatedLog, isCoolingDown = true), states.last())

            telemetryLogId.update { null }
            runCurrent()
            assertEquals(listOf(1L, 2L), completedIds)
            assertEquals(form.copy(isCoolingDown = true), states.last())
            val detachedStateCount = states.size
            secondDetail.update { TelemetryLogDetail(secondLog, null) }
            runCurrent()
            assertEquals(detachedStateCount, states.size)
            job.cancel()
            verify(exactly = 1) { repository.observeTelemetryLogDetail(1L) }
            verify(exactly = 1) { repository.observeTelemetryLogDetail(2L) }
            confirmVerified(repository)
        }
}
