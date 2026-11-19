package kurou.kodriver.domain

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * [block] を実行し、[CancellationException] 以外の例外を [captureError] へ記録して握りつぶす。
 * キャンセルは構造化並行性を保つため再スローする。
 */
public suspend fun runCapturingErrors(block: suspend () -> Unit) {
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        captureError(e)
    }
}

/**
 * [block] の結果を返し、[CancellationException] 以外の例外は [captureError] へ記録して `null` を返す。
 * 成功可否や戻り値が必要な場合に使う（`block` が `null` を返した場合と失敗を区別したいときは、非null型の値を返すこと）。
 * キャンセルは構造化並行性を保つため再スローする。
 */
public suspend fun <T : Any> runCapturingErrorsOrNull(block: suspend () -> T): T? =
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        captureError(e)
        null
    }

/** [runCapturingErrors] で包んだ [CoroutineScope.launch]。未処理例外でアプリを落とさない。 */
public fun CoroutineScope.launchCapturingErrors(block: suspend CoroutineScope.() -> Unit): Job =
    launch { runCapturingErrors { block() } }

/**
 * 各要素の [action] を [runCapturingErrors] で包んだ [onEach]。
 * 1件の失敗で `launchIn` した収集が終了し、以降の要素が処理されなくなることを防ぐ。
 */
public fun <T> Flow<T>.onEachCapturingErrors(action: suspend (T) -> Unit): Flow<T> =
    onEach { runCapturingErrors { action(it) } }
