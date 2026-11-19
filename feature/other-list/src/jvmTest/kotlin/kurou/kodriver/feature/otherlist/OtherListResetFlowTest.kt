package kurou.kodriver.feature.otherlist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.captureError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class OtherListResetFlowTest {
    private val reset: suspend () -> Unit = mockk()
    private val resetFlow = OtherListResetFlow(reset)

    @Test
    fun `未確認時と取消後の確定では初期化しない`() =
        runTest {
            assertEquals(OtherListResetUiState(), resetFlow.uiState.value)
            resetFlow.onResetConfirm()
            resetFlow.onResetClick()
            assertTrue(resetFlow.uiState.value.showResetConfirmDialog)
            resetFlow.onResetDismiss()
            assertFalse(resetFlow.uiState.value.showResetConfirmDialog)
            resetFlow.onResetConfirm()
            assertEquals(OtherListResetUiState(), resetFlow.uiState.value)
            confirmVerified(reset)
        }

    @Test
    fun `成功を通知し結果を消費できる`() =
        runTest {
            coEvery { reset() } returns Unit
            resetFlow.onResetClick()
            resetFlow.onResetConfirm()
            assertEquals(OtherListResetUiState(resetSucceeded = true), resetFlow.uiState.value)
            resetFlow.consumeResetResult()
            assertEquals(OtherListResetUiState(), resetFlow.uiState.value)
            coVerify(exactly = 1) { reset() }
            confirmVerified(reset)
        }

    @Test
    fun `実行中の再クリックと二重確定を抑止する`() =
        runTest {
            val completion = CompletableDeferred<Unit>()
            coEvery { reset() } coAnswers { completion.await() }
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
            coVerify(exactly = 1) { reset() }
            confirmVerified(reset)
        }

    @Test
    fun `通常例外を記録して失敗結果にし再実行時に前回の結果を消す`() =
        runTest {
            val error = IllegalStateException("失敗")
            val errorCapture = Class.forName("kurou.kodriver.domain.ErrorCaptureKt").kotlin
            mockkStatic(errorCapture)
            try {
                every { captureError(error) } returns Unit
                coEvery { reset() } throws error
                resetFlow.onResetClick()
                resetFlow.onResetConfirm()
                assertEquals(OtherListResetUiState(resetSucceeded = false), resetFlow.uiState.value)
                val completion = CompletableDeferred<Unit>()
                coEvery { reset() } coAnswers { completion.await() }
                resetFlow.onResetClick()
                val job = launch(start = CoroutineStart.UNDISPATCHED) { resetFlow.onResetConfirm() }
                assertTrue(resetFlow.uiState.value.isResetting)
                assertNull(resetFlow.uiState.value.resetSucceeded)
                completion.complete(Unit)
                job.join()
                assertEquals(OtherListResetUiState(resetSucceeded = true), resetFlow.uiState.value)
                coVerify(exactly = 2) { reset() }
                verify(exactly = 1) { captureError(error) }
                confirmVerified(reset, errorCapture)
            } finally {
                unmockkStatic(errorCapture)
            }
        }

    @Test
    fun `CancellationExceptionをそのまま伝播して実行中状態を解除する`() =
        runTest {
            val cancellation = CancellationException("cancelled")
            val errorCapture = Class.forName("kurou.kodriver.domain.ErrorCaptureKt").kotlin
            mockkStatic(errorCapture)
            try {
                coEvery { reset() } throws cancellation
                resetFlow.onResetClick()
                val thrown = assertFailsWith<CancellationException> { resetFlow.onResetConfirm() }
                assertSame(cancellation, thrown)
                assertEquals(OtherListResetUiState(), resetFlow.uiState.value)
                coVerify(exactly = 1) { reset() }
                verify(exactly = 0) { captureError(cancellation) }
                confirmVerified(reset, errorCapture)
            } finally {
                unmockkStatic(errorCapture)
            }
        }

    @Test
    fun `呼び出し元のキャンセルでも実行中状態を解除して結果を通知しない`() =
        runTest {
            val completion = CompletableDeferred<Unit>()
            coEvery { reset() } coAnswers { completion.await() }
            resetFlow.onResetClick()
            val job = launch(start = CoroutineStart.UNDISPATCHED) { resetFlow.onResetConfirm() }
            assertTrue(resetFlow.uiState.value.isResetting)
            job.cancelAndJoin()
            assertTrue(job.isCancelled)
            assertEquals(OtherListResetUiState(), resetFlow.uiState.value)
            coVerify(exactly = 1) { reset() }
            confirmVerified(reset)
        }
}
