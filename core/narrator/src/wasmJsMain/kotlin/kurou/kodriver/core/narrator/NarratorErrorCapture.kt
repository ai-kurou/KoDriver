package kurou.kodriver.core.narrator

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsFun

actual fun captureNarratorError(throwable: Throwable) {
    logNarratorError(throwable.toString(), throwable.stackTraceToString())
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(message, stackTrace) => console.error(message, stackTrace)")
private external fun logNarratorError(
    message: String,
    stackTrace: String,
)
