package kurou.kodriver.core.narrator

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.SourceDataLine

/** WAVのデコードと音声デバイス取得の境界。テストではFakeへ差し替える。 */
internal interface JvmAudioBackend {
    fun openStream(bytes: ByteArray): AudioInputStream

    fun getLine(format: AudioFormat): SourceDataLine
}
