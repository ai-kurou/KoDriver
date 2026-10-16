package kurou.kodriver.core.narrator

import io.sentry.Sentry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.After
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class RunCatchingTelemetryLogSaveTest {
    @After
    fun tearDown() {
        Sentry.close()
    }

    @Test
    fun `正常終了した suspend ブロックを実行する`() =
        runTest {
            var saved = false

            runCatchingTelemetryLogSave {
                yield()
                saved = true
            }

            assertEquals(true, saved)
        }

    @Test
    fun `CancellationException は同じ例外を再スローする`() =
        runTest {
            val cancellation = CancellationException("cancelled")

            val thrown =
                assertFailsWith<CancellationException> {
                    runCatchingTelemetryLogSave { throw cancellation }
                }

            assertSame(cancellation, thrown)
        }

    @Test
    fun `キャンセル以外の例外は握りつぶして記録する`() =
        runTest {
            val captured = mutableListOf<Throwable?>()
            Sentry.init { options ->
                options.dsn = "https://public@localhost/1"
                options.setBeforeSend { event, _ ->
                    captured += event.throwable
                    null
                }
            }
            val failure = IllegalStateException("save failed")

            runCatchingTelemetryLogSave { throw failure }

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
                runCatchingTelemetryLogSave { throw CancellationException("cancelled") }
            }

            assertEquals(emptyList(), captured)
        }
}
