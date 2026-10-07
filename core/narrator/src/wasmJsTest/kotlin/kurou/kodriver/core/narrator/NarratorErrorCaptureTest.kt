@file:Suppress("FunctionNaming")
@file:OptIn(ExperimentalWasmJsInterop::class)

package kurou.kodriver.core.narrator

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsFun
import kotlin.test.Test
import kotlin.test.assertEquals

class NarratorErrorCaptureTest {
    @Test
    fun `例外文字列とスタックトレースを console error に一度出力する`() {
        val throwable = RuntimeException("test", IllegalStateException("cause"))
        val calls = mutableListOf<List<String>>()
        val capture: (String, String) -> Unit = { message, stackTrace ->
            calls.add(listOf(message, stackTrace))
        }
        val originalError = replaceConsoleError(capture)
        try {
            captureNarratorError(throwable)

            assertEquals(listOf(listOf(throwable.toString(), throwable.stackTraceToString())), calls)
        } finally {
            restoreConsoleError(originalError)
        }
    }
}

@JsFun("(capture) => { const original = console.error; console.error = capture; return original; }")
private external fun replaceConsoleError(capture: (String, String) -> Unit): JsAny

@JsFun("(original) => { console.error = original; }")
private external fun restoreConsoleError(original: JsAny)
