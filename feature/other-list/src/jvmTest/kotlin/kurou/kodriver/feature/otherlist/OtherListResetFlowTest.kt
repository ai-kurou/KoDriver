package kurou.kodriver.feature.otherlist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.ResetTelemetryLogDatabaseUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class OtherListResetFlowTest {
    private val repository: TelemetryLogRepository = mockk()

    @Test
    fun `未確認時と取消後の確定では削除しない`() =
        runTest {
            val resetFlow = OtherListResetFlow(ResetTelemetryLogDatabaseUseCase(repository))
            assertEquals(OtherListResetUiState(), resetFlow.uiState.value)
            resetFlow.onResetConfirm()
            resetFlow.onResetClick()
            assertTrue(resetFlow.uiState.value.showResetConfirmDialog)
            resetFlow.onResetDismiss()
            resetFlow.onResetConfirm()
            assertEquals(OtherListResetUiState(), resetFlow.uiState.value)
            Simulator.entries.forEach { simulator ->
                coVerify(exactly = 0) { repository.deleteTelemetryLogsBySimulator(simulator) }
            }
            confirmVerified(repository)
        }

    @Test
    fun `削除中の再クリックと二重確定を抑止し成功結果を消費する`() =
        runTest {
            val completion = CompletableDeferred<Unit>()
            Simulator.entries.forEach { simulator ->
                coEvery { repository.deleteTelemetryLogsBySimulator(simulator) } coAnswers { completion.await() }
            }
            val resetFlow = OtherListResetFlow(ResetTelemetryLogDatabaseUseCase(repository))
            resetFlow.onResetClick()
            val job = launch(start = CoroutineStart.UNDISPATCHED) { resetFlow.onResetConfirm() }
            assertTrue(resetFlow.uiState.value.isResetting)
            assertFalse(resetFlow.uiState.value.showResetConfirmDialog)
            assertNull(resetFlow.uiState.value.resetSucceeded)
            resetFlow.onResetClick()
            resetFlow.onResetConfirm()
            assertFalse(resetFlow.uiState.value.showResetConfirmDialog)
            completion.complete(Unit)
            job.join()
            assertEquals(OtherListResetUiState(resetSucceeded = true), resetFlow.uiState.value)
            resetFlow.consumeResetResult()
            assertEquals(OtherListResetUiState(), resetFlow.uiState.value)
            Simulator.entries.forEach { simulator ->
                coVerify(exactly = 1) { repository.deleteTelemetryLogsBySimulator(simulator) }
            }
            confirmVerified(repository)
        }

    @Test
    fun `通常例外を失敗結果にして再実行時に前回の結果を消す`() =
        runTest {
            val firstSimulator = Simulator.entries.first()
            coEvery { repository.deleteTelemetryLogsBySimulator(firstSimulator) } throws IllegalStateException("失敗")
            val resetFlow = OtherListResetFlow(ResetTelemetryLogDatabaseUseCase(repository))
            resetFlow.onResetClick()
            resetFlow.onResetConfirm()
            assertEquals(OtherListResetUiState(resetSucceeded = false), resetFlow.uiState.value)
            val completion = CompletableDeferred<Unit>()
            Simulator.entries.forEach { simulator ->
                coEvery { repository.deleteTelemetryLogsBySimulator(simulator) } coAnswers { completion.await() }
            }
            resetFlow.onResetClick()
            val job = launch(start = CoroutineStart.UNDISPATCHED) { resetFlow.onResetConfirm() }
            assertNull(resetFlow.uiState.value.resetSucceeded)
            completion.complete(Unit)
            job.join()
            assertEquals(OtherListResetUiState(resetSucceeded = true), resetFlow.uiState.value)
            Simulator.entries.forEach { simulator ->
                coVerify(exactly = if (simulator == firstSimulator) 2 else 1) {
                    repository.deleteTelemetryLogsBySimulator(simulator)
                }
            }
            confirmVerified(repository)
        }

    @Test
    fun `CancellationExceptionをそのまま伝播して実行中状態を解除する`() =
        runTest {
            val firstSimulator = Simulator.entries.first()
            val cancellation = CancellationException("cancelled")
            coEvery { repository.deleteTelemetryLogsBySimulator(firstSimulator) } throws cancellation
            val resetFlow = OtherListResetFlow(ResetTelemetryLogDatabaseUseCase(repository))
            resetFlow.onResetClick()
            val thrown = assertFailsWith<CancellationException> { resetFlow.onResetConfirm() }
            assertSame(cancellation, thrown)
            assertEquals(OtherListResetUiState(), resetFlow.uiState.value)
            coVerify(exactly = 1) { repository.deleteTelemetryLogsBySimulator(firstSimulator) }
            confirmVerified(repository)
        }

    @Test
    fun `呼び出し元のキャンセルでも実行中状態を解除して結果を通知しない`() =
        runTest {
            val firstSimulator = Simulator.entries.first()
            val completion = CompletableDeferred<Unit>()
            coEvery { repository.deleteTelemetryLogsBySimulator(firstSimulator) } coAnswers { completion.await() }
            val resetFlow = OtherListResetFlow(ResetTelemetryLogDatabaseUseCase(repository))
            resetFlow.onResetClick()
            val job = launch(start = CoroutineStart.UNDISPATCHED) { resetFlow.onResetConfirm() }
            assertTrue(resetFlow.uiState.value.isResetting)
            job.cancelAndJoin()
            assertTrue(job.isCancelled)
            assertEquals(OtherListResetUiState(), resetFlow.uiState.value)
            coVerify(exactly = 1) { repository.deleteTelemetryLogsBySimulator(firstSimulator) }
            confirmVerified(repository)
        }
}
