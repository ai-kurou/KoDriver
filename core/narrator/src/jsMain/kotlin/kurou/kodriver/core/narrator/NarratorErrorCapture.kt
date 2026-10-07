package kurou.kodriver.core.narrator

import kotlin.js.console

actual fun captureNarratorError(throwable: Throwable) {
    console.error(throwable.toString(), throwable.stackTraceToString())
}
