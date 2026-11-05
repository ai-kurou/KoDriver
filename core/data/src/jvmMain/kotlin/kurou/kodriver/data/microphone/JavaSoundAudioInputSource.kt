package kurou.kodriver.data.microphone

import kurou.kodriver.domain.model.MICROPHONE_CHANNEL_COUNT
import kurou.kodriver.domain.model.MICROPHONE_SAMPLE_RATE_HZ
import kurou.kodriver.domain.model.MICROPHONE_SAMPLE_SIZE_BITS
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.Line
import javax.sound.sampled.LineUnavailableException
import javax.sound.sampled.Mixer
import javax.sound.sampled.TargetDataLine

/**
 * Java Sound（`javax.sound.sampled`）でマイクを開く [AudioInputSource]。
 * Windows ではデバイスごとに別の Mixer として列挙されるため、名前で選べる。
 * 同名のデバイスが複数あるときは一覧に1件として載り、開くときは名前が一致する Mixer を順に試す。
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
        if (deviceName == null) {
            openLine { AudioSystem.getLine(lineInfo) }
        } else {
            captureMixers().filter { it.name == deviceName }.firstNotNullOfOrNull { info ->
                openLine { AudioSystem.getMixer(info).getLine(lineInfo) }
            }
        }

    private fun openLine(getLine: () -> Line): AudioCapture? =
        try {
            (getLine() as? TargetDataLine)?.let(::start)
        } catch (_: LineUnavailableException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }

    /** 開始に失敗したときはラインを閉じてから失敗として扱う。 */
    private fun start(line: TargetDataLine): AudioCapture? =
        try {
            line.open(format)
            line.start()
            TargetDataLineCapture(line)
        } catch (_: LineUnavailableException) {
            line.close()
            null
        } catch (_: IllegalArgumentException) {
            line.close()
            null
        }

    /** 切断済みなどで [AudioSystem.getMixer] が拒否する Mixer は除く。 */
    private fun captureMixers(): List<Mixer.Info> =
        AudioSystem.getMixerInfo().filter { info ->
            try {
                AudioSystem.getMixer(info).isLineSupported(lineInfo)
            } catch (_: IllegalArgumentException) {
                false
            }
        }
}

private class TargetDataLineCapture(
    private val line: TargetDataLine,
) : AudioCapture {
    override fun read(buffer: ByteArray): Int = if (line.isOpen) line.read(buffer, 0, buffer.size) else -1

    /** 読み込み中の別スレッドからも呼べ、読み込みを解除する。複数回呼んでもよい。 */
    override fun close() {
        line.stop()
        line.close()
    }
}
