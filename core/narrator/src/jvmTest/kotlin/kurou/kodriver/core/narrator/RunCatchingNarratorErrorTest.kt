package kurou.kodriver.core.narrator

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class RunCatchingNarratorErrorTest {
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
    fun `キャンセル以外の例外は null を返す`() =
        runTest {
            val result =
                runCatchingNarratorError<String> {
                    throw IllegalStateException("readout failed")
                }

            assertNull(result)
        }
}
