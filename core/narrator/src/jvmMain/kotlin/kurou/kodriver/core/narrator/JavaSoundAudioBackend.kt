package kurou.kodriver.core.narrator

import java.io.ByteArrayInputStream
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

/** Java Soundを直接呼ぶ薄い実装。実デバイスの接続は実機で確認する。 */
internal class JavaSoundAudioBackend : JvmAudioBackend {
    override fun openStream(bytes: ByteArray): AudioInputStream =
        AudioSystem.getAudioInputStream(ByteArrayInputStream(bytes))

    override fun getLine(format: AudioFormat): SourceDataLine = AudioSystem.getSourceDataLine(format)
}
