package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kurou.kodriver.domain.engine.SpeechRecognitionChimePlayer
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

/**
 * デスクトップ版の確認音プレイヤー。既定の出力デバイスへ [wakeWordChimePcm] を書き出し、鳴り終わるまで待つ。
 *
 * 出力ラインの取得は [openLine] で差し替えられる（出力デバイスの無いテスト環境で検証するため）。
 */
internal class JvmSpeechRecognitionChimePlayer(
    private val openLine: () -> SourceDataLine = { AudioSystem.getSourceDataLine(CHIME_FORMAT) },
) : SpeechRecognitionChimePlayer {
    override suspend fun play() {
        val bytes = wakeWordChimePcm().toLittleEndianBytes()
        withContext(Dispatchers.IO) {
            val line = openLine()
            try {
                line.open(CHIME_FORMAT)
                line.start()
                line.write(bytes, 0, bytes.size)
                // 書き込んだ音が鳴り終わるまで待つ
                line.drain()
            } finally {
                line.close()
            }
        }
    }
}

private val CHIME_FORMAT =
    AudioFormat(WAKE_WORD_CHIME_SAMPLE_RATE.toFloat(), 16, 1, true, false)

internal fun ShortArray.toLittleEndianBytes(): ByteArray {
    val bytes = ByteArray(size * 2)
    forEachIndexed { i, sample ->
        bytes[i * 2] = (sample.toInt() and 0xFF).toByte()
        bytes[i * 2 + 1] = (sample.toInt() shr 8).toByte()
    }
    return bytes
}
