@file:Suppress("FunctionNaming")
@file:OptIn(ExperimentalWasmJsInterop::class)

package kurou.kodriver.core.narrator

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.test.Test
import kotlin.test.assertEquals

class NarratorErrorCaptureTest {
    @Test
    fun `例外文字列とスタックトレースを console error に一度出力する`() {
        val throwable = RuntimeException("test", IllegalStateException("cause"))
        val originalError = replaceConsoleError()
        try {
            captureNarratorError(throwable)

            assertEquals(1, recordedCallCount())
            assertEquals(throwable.toString(), recordedMessage())
            assertEquals(throwable.stackTraceToString(), recordedStackTrace())
        } finally {
            restoreConsoleError(originalError)
        }
    }
}

private fun replaceConsoleError(): JsAny =
    js(
        "(() => { const original = console.error; globalThis.narratorErrorCalls = []; " +
            "console.error = (message, stackTrace) => " +
            "{ globalThis.narratorErrorCalls.push([message, stackTrace]); }; " +
            "return original; })()",
    )

// 引数は js() 内の JavaScript から参照されるため、detekt からは未使用に見える。
@Suppress("UnusedParameter")
private fun restoreConsoleError(original: JsAny): Unit =
    js(
        "{ console.error = original; delete globalThis.narratorErrorCalls; }",
    )

private fun recordedCallCount(): Int = js("globalThis.narratorErrorCalls.length")

private fun recordedMessage(): String = js("globalThis.narratorErrorCalls[0][0]")

private fun recordedStackTrace(): String = js("globalThis.narratorErrorCalls[0][1]")
