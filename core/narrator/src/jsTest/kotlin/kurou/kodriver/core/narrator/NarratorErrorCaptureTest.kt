@file:Suppress("FunctionNaming")

package kurou.kodriver.core.narrator

import kotlin.test.Test
import kotlin.test.assertEquals

class NarratorErrorCaptureTest {
    @Test
    fun `例外文字列とスタックトレースを console error に一度出力する`() {
        val throwable = RuntimeException("test", IllegalStateException("cause"))
        val calls = mutableListOf<List<String>>()
        val console = js("console")
        val originalError = console.error
        console.error = { message: String, stackTrace: String ->
            calls.add(listOf(message, stackTrace))
            Unit
        }
        try {
            captureNarratorError(throwable)

            assertEquals(listOf(listOf(throwable.toString(), throwable.stackTraceToString())), calls)
        } finally {
            console.error = originalError
        }
    }
}
