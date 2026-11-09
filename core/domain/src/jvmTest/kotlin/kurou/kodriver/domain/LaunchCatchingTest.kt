package kurou.kodriver.domain

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LaunchCatchingTest {
    private val errorCapture = Class.forName("kurou.kodriver.domain.ErrorCaptureKt").kotlin

    @Test
    fun `例外なしならブロックを実行し記録しない`() =
        runTest {
            val captured = slot<Throwable>()
            mockkStatic(errorCapture)
            every { captureError(capture(captured)) } returns Unit
            try {
                var executed = false
                runCapturingErrors { executed = true }

                assertTrue(executed)
                assertTrue(!captured.isCaptured)
            } finally {
                unmockkStatic(errorCapture)
            }
        }

    @Test
    fun `通常の例外は記録して握りつぶす`() =
        runTest {
            val error = IllegalStateException("fail")
            val captured = slot<Throwable>()
            mockkStatic(errorCapture)
            every { captureError(capture(captured)) } returns Unit
            try {
                runCapturingErrors { throw error }

                assertSame(error, captured.captured)
                verify(exactly = 1) { captureError(error) }
            } finally {
                unmockkStatic(errorCapture)
            }
        }

    @Test
    fun `キャンセル例外は再スローし記録しない`() =
        runTest {
            val captured = slot<Throwable>()
            mockkStatic(errorCapture)
            every { captureError(capture(captured)) } returns Unit
            try {
                assertFailsWith<CancellationException> { runCapturingErrors { throw CancellationException("cancel") } }

                assertTrue(!captured.isCaptured)
            } finally {
                unmockkStatic(errorCapture)
            }
        }

    @Test
    fun `launchCapturingErrorsは例外後もスコープを継続させる`() =
        runTest(UnconfinedTestDispatcher()) {
            val error = IllegalStateException("fail")
            mockkStatic(errorCapture)
            every { captureError(error) } returns Unit
            try {
                var count = 0
                launchCapturingErrors { throw error }
                launchCapturingErrors { count++ }

                assertEquals(1, count)
                verify(exactly = 1) { captureError(error) }
            } finally {
                unmockkStatic(errorCapture)
            }
        }

    @Test
    fun `onEachCapturingErrorsは1要素の失敗後も後続の要素を処理する`() =
        runTest(UnconfinedTestDispatcher()) {
            val error = IllegalStateException("fail")
            mockkStatic(errorCapture)
            every { captureError(error) } returns Unit
            try {
                val processed = mutableListOf<Int>()
                flowOf(1, 2, 3)
                    .onEachCapturingErrors {
                        if (it == 2) throw error
                        processed += it
                    }.launchIn(this)

                assertEquals(listOf(1, 3), processed)
                verify(exactly = 1) { captureError(error) }
                confirmVerified(errorCapture)
            } finally {
                unmockkStatic(errorCapture)
            }
        }
}
