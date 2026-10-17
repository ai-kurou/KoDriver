package kurou.kodriver.core.narrator

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.DataLine
import javax.sound.sampled.Line
import javax.sound.sampled.LineListener
import javax.sound.sampled.SourceDataLine

/** 再生テストで使わないJava Soundの補助APIを実装するStub。再生操作はFakeが実装する。 */
internal abstract class StubSourceDataLine(
    private val audioFormat: AudioFormat,
) : SourceDataLine {
    override fun isActive(): Boolean = isRunning

    override fun getFormat(): AudioFormat = audioFormat

    override fun getLineInfo(): Line.Info = DataLine.Info(SourceDataLine::class.java, audioFormat)

    override fun open(
        format: AudioFormat,
        bufferSize: Int,
    ) {
        open(format)
    }

    override fun open() {
        open(audioFormat)
    }

    override fun flush() = Unit

    override fun available(): Int = 8192

    override fun getBufferSize(): Int = 8192

    override fun getFramePosition(): Int = 0

    override fun getLongFramePosition(): Long = 0

    override fun getMicrosecondPosition(): Long = 0

    override fun getLevel(): Float = 0f

    override fun addLineListener(listener: LineListener) = Unit

    override fun removeLineListener(listener: LineListener) = Unit
}
