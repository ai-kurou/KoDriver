package kurou.kodriver.core.narrator

import java.io.ByteArrayInputStream
import java.io.IOException
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.Control
import javax.sound.sampled.FloatControl
import javax.sound.sampled.SourceDataLine

internal class FakeJvmAudioBackend : JvmAudioBackend {
    val format = AudioFormat(1000f, 16, 1, true, false)
    var line = FakeSourceDataLine(format)
    var samples = byteArrayOf(1, 2, 3, 4, 5, 6)
    var openedBytes: ByteArray? = null
    var requestedFormat: AudioFormat? = null
    var failureAt: String? = null
    val failure = IOException("audio failed")
    var streamClosed = false
    var onRead: () -> Unit = {}

    override fun openStream(bytes: ByteArray): AudioInputStream {
        openedBytes = bytes
        if (failureAt == "decode") throw failure
        val input =
            object : ByteArrayInputStream(samples) {
                override fun close() {
                    streamClosed = true
                    if (failureAt == "streamClose") throw failure
                    super.close()
                }
            }
        return object : AudioInputStream(input, format, (samples.size / format.frameSize).toLong()) {
            override fun read(
                bytes: ByteArray,
                offset: Int,
                length: Int,
            ): Int {
                if (failureAt == "read") throw failure
                val count = super.read(bytes, offset, length)
                onRead()
                return count
            }
        }
    }

    override fun getLine(format: AudioFormat): SourceDataLine {
        requestedFormat = format
        if (failureAt == "getLine") throw failure
        return line
    }
}

internal class FakeSourceDataLine(
    audioFormat: AudioFormat,
    minimumGain: Float = -80f,
) : StubSourceDataLine(audioFormat) {
    val calls = mutableListOf<String>()
    val writes = mutableListOf<ByteArray>()
    val failure = IOException("line failed")
    val gain =
        object : FloatControl(Type.MASTER_GAIN, minimumGain, 6f, 0.1f, 1, 0f, "dB") {
            override fun setValue(value: Float) {
                record("gain")
                super.setValue(value)
            }
        }
    var supportsGain = true
    var failures = emptySet<String>()
    var onWrite: () -> Unit = {}
    var openedFormat: AudioFormat? = null
    var running = false
    private var opened = false

    private fun record(call: String) {
        calls.add(call)
        if (call in failures) throw failure
    }

    override fun open(format: AudioFormat) {
        record("open")
        openedFormat = format
        opened = true
    }

    override fun start() {
        record("start")
        running = true
    }

    override fun write(
        bytes: ByteArray,
        offset: Int,
        length: Int,
    ): Int {
        record("write")
        writes.add(bytes.copyOfRange(offset, offset + length))
        onWrite()
        return length
    }

    override fun drain() {
        record("drain")
    }

    override fun stop() {
        record("stop")
        running = false
    }

    override fun close() {
        record("close")
        opened = false
        running = false
    }

    override fun isControlSupported(control: Control.Type): Boolean {
        record("supportsGain")
        return supportsGain && control == FloatControl.Type.MASTER_GAIN
    }

    override fun getControl(control: Control.Type): Control {
        record("getGain")
        require(control == FloatControl.Type.MASTER_GAIN)
        return gain
    }

    override fun isRunning(): Boolean = running

    override fun isOpen(): Boolean = opened

    override fun getControls(): Array<Control> = arrayOf(gain)
}
