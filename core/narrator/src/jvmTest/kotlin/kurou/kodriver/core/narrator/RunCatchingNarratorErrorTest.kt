package kurou.kodriver.core.narrator

import io.sentry.Sentry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.After
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class RunCatchingNarratorErrorTest {
    @After
    fun tearDown() {
        Sentry.close()
    }

    @Test
    fun `正常終了した suspend ブロックの値を返す`() =
        runTest {
            val result =
                runCatchingNarratorError {
                    yield()
                    "読み上げ文言"
                }

            assertEquals("読み上げ文言", result)
        }

    @Test
    fun `CancellationException は同じ例外を再スローする`() =
        runTest {
            val cancellation = CancellationException("cancelled")

            val thrown =
                assertFailsWith<CancellationException> {
                    runCatchingNarratorError<String> { throw cancellation }
                }

            assertSame(cancellation, thrown)
        }

    @Test
    fun `キャンセル以外の例外は null を返し記録する`() =
        runTest {
            val captured = mutableListOf<Throwable?>()
            Sentry.init { options ->
                options.dsn = "https://public@localhost/1"
                options.setBeforeSend { event, _ ->
                    captured += event.throwable
                    null
                }
            }
            val failure = IllegalStateException("readout failed")

            val result = runCatchingNarratorError<String> { throw failure }

            assertNull(result)
            assertEquals(1, captured.size)
            assertSame(failure, captured.single())
        }

    @Test
    fun `CancellationException は記録しない`() =
        runTest {
            val captured = mutableListOf<Throwable?>()
            Sentry.init { options ->
                options.dsn = "https://public@localhost/1"
                options.setBeforeSend { event, _ ->
                    captured += event.throwable
                    null
                }
            }

            assertFailsWith<CancellationException> {
                runCatchingNarratorError<String> { throw CancellationException("cancelled") }
            }

            assertEquals(emptyList(), captured)
        }
}
