package kurou.kodriver.feature.otherlist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.StateFlow
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

class OtherListResetFlowTest : OtherListResetFlowTestContract() {
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

    private val resetFlow = OtherListResetFlow(ResetTelemetryLogDatabaseUseCase(repository))

    override val uiState: StateFlow<OtherListResetUiState> = resetFlow.uiState

    override fun onResetClick() = resetFlow.onResetClick()

    override suspend fun onResetConfirm() = resetFlow.onResetConfirm()

    override fun stubFailure(exception: Exception) {
        coEvery { repository.deleteTelemetryLogsBySimulator(Simulator.entries.first()) } throws exception
    }

    override fun stubAwait(completion: CompletableDeferred<Unit>) {
        Simulator.entries.forEach { simulator ->
            coEvery { repository.deleteTelemetryLogsBySimulator(simulator) } coAnswers { completion.await() }
        }
    }

    override fun verifyCalls(
        firstCalls: Int,
        remainingCalls: Int,
    ) {
        Simulator.entries.forEach { simulator ->
            coVerify(exactly = if (simulator == Simulator.entries.first()) firstCalls else remainingCalls) {
                repository.deleteTelemetryLogsBySimulator(simulator)
            }
        }
        confirmVerified(repository)
    }
}

/** 両方の初期化Flowで例外・キャンセル時の契約を同じテストとして実行する。 */
abstract class OtherListResetFlowTestContract {
    protected abstract val uiState: StateFlow<OtherListResetUiState>

    protected abstract fun onResetClick()

    protected abstract suspend fun onResetConfirm()

    protected abstract fun stubFailure(exception: Exception)

    protected abstract fun stubAwait(completion: CompletableDeferred<Unit>)

    protected abstract fun verifyCalls(
        firstCalls: Int,
        remainingCalls: Int,
    )

    @Test
    fun `通常例外を失敗結果にして再実行時に前回の結果を消す`() =
        runTest {
            stubFailure(IllegalStateException("失敗"))
            onResetClick()
            onResetConfirm()
            assertEquals(OtherListResetUiState(resetSucceeded = false), uiState.value)
            val completion = CompletableDeferred<Unit>()
            stubAwait(completion)
            onResetClick()
            val job = launch(start = CoroutineStart.UNDISPATCHED) { onResetConfirm() }
            assertTrue(uiState.value.isResetting)
            assertNull(uiState.value.resetSucceeded)
            completion.complete(Unit)
            job.join()
            assertEquals(OtherListResetUiState(resetSucceeded = true), uiState.value)
            verifyCalls(firstCalls = 2, remainingCalls = 1)
        }

    @Test
    fun `CancellationExceptionをそのまま伝播して実行中状態を解除する`() =
        runTest {
            val cancellation = CancellationException("cancelled")
            stubFailure(cancellation)
            onResetClick()
            val thrown = assertFailsWith<CancellationException> { onResetConfirm() }
            assertSame(cancellation, thrown)
            assertEquals(OtherListResetUiState(), uiState.value)
            verifyCalls(firstCalls = 1, remainingCalls = 0)
        }

    @Test
    fun `呼び出し元のキャンセルでも実行中状態を解除して結果を通知しない`() =
        runTest {
            val completion = CompletableDeferred<Unit>()
            stubAwait(completion)
            onResetClick()
            val job = launch(start = CoroutineStart.UNDISPATCHED) { onResetConfirm() }
            assertTrue(uiState.value.isResetting)
            job.cancelAndJoin()
            assertTrue(job.isCancelled)
            assertEquals(OtherListResetUiState(), uiState.value)
            verifyCalls(firstCalls = 1, remainingCalls = 0)
        }
}
