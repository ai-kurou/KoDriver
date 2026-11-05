package kurou.kodriver.data.microphone

internal class FakeAudioInputSource(
    private val names: List<String> = emptyList(),
    private val capture: AudioCapture? = null,
) : AudioInputSource {
    val openedNames = mutableListOf<String?>()

    override fun deviceNames(): List<String> = names

    override fun open(deviceName: String?): AudioCapture? {
        openedNames += deviceName
        return capture
    }
}

/** [reads] を順に返し、尽きたら -1 を返す。 */
internal class FakeAudioCapture(
    private val reads: List<ByteArray>,
) : AudioCapture {
    private var index = 0
    var closed = false
        private set

    override fun read(buffer: ByteArray): Int {
        if (index >= reads.size) return -1
        val data = reads[index++]
        data.copyInto(buffer)
        return data.size
    }

    override fun close() {
        closed = true
    }
}

/** [close] されるまで [read] がブロックし、閉じられると -1 を返す。 */
internal class BlockingAudioCapture : AudioCapture {
    val readStarted = java.util.concurrent.CountDownLatch(1)
    private val closedLatch = java.util.concurrent.CountDownLatch(1)

    val closed: Boolean get() = closedLatch.count == 0L

    override fun read(buffer: ByteArray): Int {
        readStarted.countDown()
        closedLatch.await()
        return -1
    }

    override fun close() {
        closedLatch.countDown()
    }
}
