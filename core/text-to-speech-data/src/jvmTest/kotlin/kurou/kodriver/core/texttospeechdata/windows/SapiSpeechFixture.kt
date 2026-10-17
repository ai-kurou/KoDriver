package kurou.kodriver.core.texttospeechdata.windows

import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.assertFalse
import kotlin.test.assertNull

/** 待機に入ったFutureを観測し、sleepなしで発話の順序を制御する。 */
internal class SapiSpeechFixture : AutoCloseable {
    val process = FakeResidentSpeechProcess()
    private val calls = mutableListOf<SpeechCall>()
    private val awaited = LinkedBlockingQueue<CompletableFuture<Unit>>()
    val synthesizer =
        SapiSpeechSynthesizer(
            isWindows = true,
            startProcess = { process },
            awaitCompletion = {
                awaited.add(it)
                it.get()
            },
        )

    fun speak(
        text: String,
        queue: Boolean = false,
    ): SpeechCall = SpeechCall { synthesizer.speak(text, queue, 80, "voice", 1.5f, 1.2f) }.also { calls.add(it) }

    fun awaitCompletion(): CompletableFuture<Unit> = requireNotNull(awaited.poll(5, TimeUnit.SECONDS))

    override fun close() {
        process.finishAndJoinReader()
        calls.forEach { it.join() }
    }
}

internal class SpeechCall(
    action: () -> Unit,
) {
    private val result = CompletableFuture<Throwable?>()
    private val thread =
        Thread {
            try {
                action()
                result.complete(null)
            } catch (e: Exception) {
                result.complete(e)
            }
        }.also { it.start() }

    fun awaitSuccess() {
        assertNull(result.get(5, TimeUnit.SECONDS))
        join()
    }

    fun awaitFailure(): Throwable = requireNotNull(result.get(5, TimeUnit.SECONDS)).also { join() }

    fun interrupt() {
        thread.interrupt()
    }

    fun join() {
        thread.join(5000)
        assertFalse(thread.isAlive, "読み上げThreadが終了しませんでした")
    }
}
