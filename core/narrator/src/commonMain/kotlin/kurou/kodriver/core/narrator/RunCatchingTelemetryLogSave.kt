package kurou.kodriver.core.narrator

import kotlinx.coroutines.CancellationException

/**
 * ログ保存は読み上げの補助機能のため、保存失敗で以後の読み上げを止めず、例外の記録のみ行う。
 * キャンセルは再スローする。
 */
suspend fun runCatchingTelemetryLogSave(block: suspend () -> Unit) {
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        captureNarratorError(e)
    }
}
