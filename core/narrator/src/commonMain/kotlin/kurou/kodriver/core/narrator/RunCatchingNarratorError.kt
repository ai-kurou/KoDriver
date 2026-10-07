package kurou.kodriver.core.narrator

import kotlinx.coroutines.CancellationException

/** キャンセルは再スローし、それ以外の例外は記録して null を返す。 */
suspend inline fun <T> runCatchingNarratorError(block: suspend () -> T): T? =
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        captureNarratorError(e)
        null
    }
