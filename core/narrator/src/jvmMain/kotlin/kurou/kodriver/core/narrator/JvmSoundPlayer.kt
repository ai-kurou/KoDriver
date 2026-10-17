package kurou.kodriver.core.narrator

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.FloatControl
import javax.sound.sampled.SourceDataLine

/**
 * WAV 音声を再生する Jvm 向け実装。
 */
class JvmSoundPlayer internal constructor(
    private val audio: JvmAudioBackend,
    private val ioDispatcher: CoroutineDispatcher,
    private val captureError: (Throwable) -> Unit,
) : SoundPlayer {
    constructor() : this(JavaSoundAudioBackend(), Dispatchers.IO, ::captureNarratorError)

    @Volatile
    private var currentLine: SourceDataLine? = null

    override val isPlaying: Boolean
        get() = currentLine?.isRunning == true

    override suspend fun play(
        bytes: ByteArray,
        volume: Int,
    ) {
        try {
            audio.openStream(bytes).use { stream ->
                val line = audio.getLine(stream.format)
                try {
                    line.use {
                        line.open(stream.format)
                        // useを重ね、停止や解放の失敗で元の例外やキャンセルを上書きしない。
                        AutoCloseable { line.stop() }.use {
                            applyVolume(line, volume)
                            line.start()
                            currentLine = line
                            writeStream(stream, line)
                        }
                    }
                } finally {
                    currentLine = null
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            captureError(e)
        }
    }

    private suspend fun writeStream(
        stream: AudioInputStream,
        line: SourceDataLine,
    ) {
        withContext(ioDispatcher) {
            val buf = ByteArray(8192)
            var n: Int
            while (stream.read(buf).also { n = it } != -1) {
                ensureActive()
                line.write(buf, 0, n)
            }
            ensureActive()
            // Bluetooth A2DPの伝送バッファ分を押し流すため、末尾に0.3秒分の無音を書く。
            val format = stream.format
            val silenceFrames = (format.frameRate * BLUETOOTH_TAIL_SILENCE_SEC).toInt()
            val silence = ByteArray(silenceFrames * format.frameSize)
            line.write(silence, 0, silence.size)
            line.drain()
        }
    }

    private fun applyVolume(
        line: SourceDataLine,
        volume: Int,
    ) {
        if (!line.isControlSupported(FloatControl.Type.MASTER_GAIN)) return
        val gainControl = line.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
        val gainDb =
            if (volume <= 0) {
                gainControl.minimum
            } else {
                (20.0 * kotlin.math.log10(volume / 100.0))
                    .toFloat()
                    .coerceIn(gainControl.minimum, gainControl.maximum)
            }
        gainControl.value = gainDb
    }

    companion object {
        private const val BLUETOOTH_TAIL_SILENCE_SEC = 0.3f
    }
}
