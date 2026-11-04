package kurou.kodriver.data.microphone

import kurou.kodriver.domain.model.MICROPHONE_CHANNEL_COUNT
import kurou.kodriver.domain.model.MICROPHONE_SAMPLE_RATE_HZ
import kurou.kodriver.domain.model.MICROPHONE_SAMPLE_SIZE_BITS
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.LineUnavailableException
import javax.sound.sampled.Mixer
import javax.sound.sampled.TargetDataLine

/**
 * Java Sound（`javax.sound.sampled`）でマイクを開く [AudioInputSource]。
 * Windows ではデバイスごとに別の Mixer として列挙されるため、名前で選べる。
 */
internal class JavaSoundAudioInputSource : AudioInputSource {
    private val format =
        AudioFormat(
            MICROPHONE_SAMPLE_RATE_HZ.toFloat(),
            MICROPHONE_SAMPLE_SIZE_BITS,
            MICROPHONE_CHANNEL_COUNT,
            true,
            false,
        )
    private val lineInfo = DataLine.Info(TargetDataLine::class.java, format)

    override fun deviceNames(): List<String> = captureMixers().map { it.name }.distinct()

    override fun open(deviceName: String?): AudioCapture? =
        try {
            val line =
                if (deviceName == null) {
                    AudioSystem.getLine(lineInfo)
                } else {
                    val info = captureMixers().firstOrNull { it.name == deviceName }
                    info?.let { AudioSystem.getMixer(it).getLine(lineInfo) }
                }
            (line as TargetDataLine?)?.let { target ->
                target.open(format)
                target.start()
                TargetDataLineCapture(target)
            }
        } catch (_: LineUnavailableException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }

    private fun captureMixers(): List<Mixer.Info> =
        AudioSystem.getMixerInfo().filter { AudioSystem.getMixer(it).isLineSupported(lineInfo) }
}

private class TargetDataLineCapture(
    private val line: TargetDataLine,
) : AudioCapture {
    override fun read(buffer: ByteArray): Int = if (line.isOpen) line.read(buffer, 0, buffer.size) else -1

    override fun close() {
        line.stop()
        line.close()
    }
}
