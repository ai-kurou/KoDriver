package kurou.kodriver

import kotlin.coroutines.cancellation.CancellationException

/**
 * サーバーを起動し、失敗した場合は停止してから起動時の例外を投げ直す。
 *
 * 起動に失敗してもアプリは動き続けるため、確保済みのスレッドを解放する。
 * 停止にも失敗した場合は、起動失敗の原因を失わないよう、停止の例外を抑制例外として付ける。
 */
@Suppress("TooGenericExceptionCaught")
internal fun KoDriverServer.startOrStop() {
    try {
        start()
    } catch (e: CancellationException) {
        throw e
    } catch (startError: Exception) {
        try {
            stop()
        } catch (stopError: Exception) {
            startError.addSuppressed(stopError)
        }
        throw startError
    }
}
