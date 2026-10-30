package kurou.kodriver.core.texttospeechdata.windows

import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** [finish] または [destroy] が呼ばれるまで [waitFor] がブロックする読み上げプロセス。 */
internal class FakeSayProcess(
    private val exitCode: Int = 0,
) : Process() {
    private val finished = CountDownLatch(1)
    private val waiting = CountDownLatch(1)

    @Volatile
    var destroyed = false

    fun awaitWaiting() = waiting.await(5, TimeUnit.SECONDS)

    fun finish() = finished.countDown()

    override fun getInputStream(): InputStream = InputStream.nullInputStream()

    override fun getErrorStream(): InputStream = InputStream.nullInputStream()

    override fun getOutputStream(): OutputStream = OutputStream.nullOutputStream()

    override fun waitFor(): Int {
        waiting.countDown()
        finished.await()
        return exitValue()
    }

    override fun exitValue(): Int = if (destroyed) DESTROYED_EXIT_CODE else exitCode

    override fun destroy() {
        destroyed = true
        finished.countDown()
    }

    private companion object {
        const val DESTROYED_EXIT_CODE = 143
    }
}
