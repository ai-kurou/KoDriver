package kurou.kodriver.core.windowshotkeydata

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import java.util.concurrent.LinkedBlockingQueue

internal class FakeHotkeyNativeApi(
    private val registrationResult: Boolean = true,
    private val messageFailure: Exception? = null,
    private val preparationFailure: Exception? = null,
) : HotkeyNativeApi {
    private val messages = LinkedBlockingQueue<HotkeyMessage>()
    val registered = CompletableDeferred<Unit>()
    val preparing = CompletableDeferred<Unit>()
    var preparationGate: CompletableDeferred<Unit>? = null
    var registeredHotkey: GlobalHotkey? = null
    var registrationThread: Thread? = null
    var unregisterThread: Thread? = null
    var unregisterCount = 0
    val quitThreadIds = mutableListOf<Int>()

    override fun prepareMessageQueue(): Int {
        preparationFailure?.let { throw it }
        preparing.complete(Unit)
        preparationGate?.let { gate -> runBlocking { gate.await() } }
        return THREAD_ID
    }

    override fun register(hotkey: GlobalHotkey): Boolean {
        registeredHotkey = hotkey
        registrationThread = Thread.currentThread()
        registered.complete(Unit)
        return registrationResult
    }

    override fun nextMessage(): HotkeyMessage? {
        messageFailure?.let { throw it }
        val message = messages.take()
        return if (message.message == QUIT_MESSAGE) null else message
    }

    override fun unregister() {
        unregisterThread = Thread.currentThread()
        unregisterCount++
    }

    override fun postQuit(threadId: Int) {
        quitThreadIds.add(threadId)
        finish()
    }

    fun send(message: HotkeyMessage) {
        messages.put(message)
    }

    fun finish() {
        send(HotkeyMessage(QUIT_MESSAGE, 0))
    }

    companion object {
        const val THREAD_ID = 42
        private const val QUIT_MESSAGE = 0x0012
    }
}
