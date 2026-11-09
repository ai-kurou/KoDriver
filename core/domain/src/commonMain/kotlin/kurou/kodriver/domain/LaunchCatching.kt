package kurou.kodriver.domain

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * [block] を実行し、[CancellationException] 以外の例外を [captureError] へ記録して握りつぶす。
 * キャンセルは構造化並行性を保つため再スローする。
 */
suspend fun runCapturingErrors(block: suspend () -> Unit) {
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        captureError(e)
    }
}

/** [runCapturingErrors] で包んだ [CoroutineScope.launch]。未処理例外でアプリを落とさない。 */
fun CoroutineScope.launchCapturingErrors(block: suspend CoroutineScope.() -> Unit): Job =
    launch { runCapturingErrors { block() } }
