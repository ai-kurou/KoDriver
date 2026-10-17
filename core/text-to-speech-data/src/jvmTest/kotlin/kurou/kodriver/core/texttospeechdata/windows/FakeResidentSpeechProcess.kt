package kurou.kodriver.core.texttospeechdata.windows

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** 外部プロセスを起動せず、応答・EOF・入出力失敗をテストから制御する。 */
internal class FakeResidentSpeechProcess : Process() {
    private val events = LinkedBlockingQueue<ReadEvent>()
    private val readerStarted = CountDownLatch(1)
    private val destroyed = CountDownLatch(1)
    private var readerThread: Thread? = null
    private val written = ByteArrayOutputStream()

    @Volatile
    var alive = true
    var failWrite = false
    var failFlush = false
    var flushCount = 0
        private set

    val requests: String
        get() = written.toString(Charsets.US_ASCII.name())

    private val responses =
        object : InputStream() {
            override fun read(): Int {
                readerThread = Thread.currentThread()
                readerStarted.countDown()
                while (true) {
                    when (val event = events.take()) {
                        is ReadEvent.Byte -> return event.value
                        is ReadEvent.Failure -> throw event.error
                        is ReadEvent.Barrier -> event.reached.countDown()
                    }
                }
            }

            // 追加の応答を待たず1バイトずつ返し、BufferedReaderの行処理を進める。
            override fun read(
                bytes: ByteArray,
                offset: Int,
                length: Int,
            ): Int {
                if (length == 0) return 0
                val value = read()
                if (value == -1) return -1
                bytes[offset] = value.toByte()
                return 1
            }
        }

    private val commands =
        object : OutputStream() {
            override fun write(value: Int) {
                if (failWrite) throw IOException("write failed")
                written.write(value)
            }

            override fun flush() {
                if (failFlush) throw IOException("flush failed")
                flushCount++
            }
        }

    fun respond(line: String) {
        (line + "\n").toByteArray(Charsets.UTF_8).forEach { events.add(ReadEvent.Byte(it.toInt() and 0xff)) }
    }

    /** 先行する応答行の処理が終わり、次の入力を読み始めるまで待つ。 */
    fun awaitResponses() {
        val reached = CountDownLatch(1)
        events.add(ReadEvent.Barrier(reached))
        assertTrue(reached.await(5, TimeUnit.SECONDS), "応答行が処理されませんでした")
    }

    fun finish() {
        events.add(ReadEvent.Byte(-1))
    }

    fun failRead() {
        events.add(ReadEvent.Failure(IOException("read failed")))
    }

    fun awaitDestroyed() {
        assertTrue(destroyed.await(5, TimeUnit.SECONDS), "プロセスが破棄されませんでした")
    }

    fun finishAndJoinReader() {
        finish()
        assertTrue(readerStarted.await(5, TimeUnit.SECONDS), "読み取りThreadが起動しませんでした")
        val reader = requireNotNull(readerThread)
        reader.join(5000)
        assertFalse(reader.isAlive, "読み取りThreadが終了しませんでした")
    }

    override fun getOutputStream(): OutputStream = commands

    override fun getInputStream(): InputStream = responses

    override fun getErrorStream(): InputStream = InputStream.nullInputStream()

    override fun isAlive(): Boolean = alive

    override fun destroy() {
        alive = false
        finish()
        destroyed.countDown()
    }

    override fun waitFor(): Int {
        destroyed.await()
        return 0
    }

    override fun exitValue(): Int {
        check(!alive) { "process still running" }
        return 0
    }

    private sealed interface ReadEvent {
        data class Byte(
            val value: Int,
        ) : ReadEvent

        data class Failure(
            val error: IOException,
        ) : ReadEvent

        data class Barrier(
            val reached: CountDownLatch,
        ) : ReadEvent
    }
}
