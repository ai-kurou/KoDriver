package kurou.kodriver.core.windowshotkeydata

import io.sentry.Sentry
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext
import kotlin.concurrent.thread

/**
 * 購読ごとに専用スレッドを作り、登録と解除を同じスレッドで行う。
 * キャンセル時はキューの準備を待って WM_QUIT を投稿し、解除とスレッドの終了を待つ。
 */
class WindowsGlobalHotkeySource internal constructor(
    private val nativeApi: HotkeyNativeApi,
) : GlobalHotkeySource {
    override fun observe(hotkey: GlobalHotkey): Flow<Unit> =
        callbackFlow {
            val ready = CompletableDeferred<Int?>()
            val lifecycleLock = Any()
            var finished = false
            val messageThread =
                thread(name = "KoDriver-global-hotkey") {
                    var registered = false
                    try {
                        val threadId = nativeApi.prepareMessageQueue()
                        registered = nativeApi.register(hotkey)
                        if (!registered) {
                            Sentry.captureMessage("WindowsGlobalHotkeySource: RegisterHotKey failed for '$hotkey'")
                            return@thread
                        }
                        ready.complete(threadId)
                        while (true) {
                            val message = nativeApi.nextMessage() ?: break
                            if (message.message == WM_HOTKEY && message.hotkeyId == HOTKEY_ID) {
                                trySend(Unit)
                            }
                        }
                    } catch (e: Exception) {
                        close(e)
                    } finally {
                        synchronized(lifecycleLock) {
                            finished = true
                            if (registered) nativeApi.unregister()
                        }
                        ready.complete(null)
                        close()
                    }
                }
            try {
                awaitClose { }
            } finally {
                withContext(NonCancellable + Dispatchers.IO) {
                    val threadId = ready.await()
                    synchronized(lifecycleLock) {
                        if (threadId != null && !finished) nativeApi.postQuit(threadId)
                    }
                    messageThread.join()
                }
            }
        }.buffer(Channel.UNLIMITED)

    companion object {
        /** 非 Windows 環境ではネイティブ API を生成せず、即座に完了する実装を返す。 */
        fun create(osName: String = System.getProperty("os.name")): GlobalHotkeySource =
            create(osName) { JnaHotkeyNativeApi() }

        internal fun create(
            osName: String,
            nativeApiFactory: () -> HotkeyNativeApi,
        ): GlobalHotkeySource =
            if (osName.contains("Windows", ignoreCase = true)) {
                WindowsGlobalHotkeySource(nativeApiFactory())
            } else {
                object : GlobalHotkeySource {
                    override fun observe(hotkey: GlobalHotkey): Flow<Unit> = emptyFlow()
                }
            }
    }
}
