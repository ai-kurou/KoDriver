package kurou.kodriver.core.narrator

actual fun captureNarratorError(throwable: Throwable) {
    logNarratorError(throwable.toString(), throwable.stackTraceToString())
}

// 引数は js() 内の JavaScript から参照されるため、detekt からは未使用に見える。
@Suppress("UnusedParameter")
private fun logNarratorError(
    message: String,
    stackTrace: String,
): Unit = js("console.error(message, stackTrace)")
