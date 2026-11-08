package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kurou.kodriver.domain.engine.SpeechRecognitionChimePlayer
import kurou.kodriver.domain.model.SpeechRecognitionChime
import java.util.concurrent.atomic.AtomicBoolean
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

/**
 * デスクトップ版の確認音プレイヤー。既定の出力デバイスへ確認音の波形（[SpeechRecognitionChime.pcm]）を書き出し、鳴り終わるまで待つ。
 *
 * 再生中に取り消されたら、出力ラインを止めて（`stop` / `flush`）ブロック中の書き込み・待機を戻らせ、ラインを閉じる。
 * 出力ラインの取得は [openLine] で差し替えられる（出力デバイスの無いテスト環境で検証するため）。
 */
internal class JvmSpeechRecognitionChimePlayer(
    private val openLine: () -> SourceDataLine = { AudioSystem.getSourceDataLine(CHIME_FORMAT) },
) : SpeechRecognitionChimePlayer {
    override suspend fun play(chime: SpeechRecognitionChime) {
        val bytes = chime.pcm().toLittleEndianBytes()
        withContext(Dispatchers.IO) {
            val line = openLine()
            val finished = AtomicBoolean(false)
            try {
                line.open(CHIME_FORMAT)
                line.start()
                // write と drain は取り消しに応じないため、取り消されたら別スレッドから出力を止めて戻らせる
                // （読み上げの開始で打ち切られた確認音が、読み上げの頭に重なって鳴り続けないようにする）
                coroutineScope {
                    val watcher =
                        launch(start = CoroutineStart.UNDISPATCHED) {
                            try {
                                awaitCancellation()
                            } finally {
                                if (!finished.get()) {
                                    line.stop()
                                    line.flush()
                                }
                            }
                        }
                    try {
                        line.write(bytes, 0, bytes.size)
                        // 書き込んだ音が鳴り終わるまで待つ
                        line.drain()
                    } finally {
                        finished.set(true)
                        watcher.cancel()
                    }
                }
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
