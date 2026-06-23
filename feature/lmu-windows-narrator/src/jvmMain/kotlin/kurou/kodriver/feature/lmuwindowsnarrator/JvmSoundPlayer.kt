package kurou.kodriver.feature.lmuwindowsnarrator

import io.sentry.Sentry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.FloatControl
import javax.sound.sampled.SourceDataLine

class JvmSoundPlayer : SoundPlayer {
    private var currentLine: SourceDataLine? = null

    override val isPlaying: Boolean
        get() = currentLine?.isRunning == true

    override suspend fun play(bytes: ByteArray, volume: Int) {
        try {
            val stream = AudioSystem.getAudioInputStream(ByteArrayInputStream(bytes))
            val format = stream.format
            val line = AudioSystem.getSourceDataLine(format)
            line.open(format)
            applyVolume(line, volume)
            line.start()
            currentLine = line
            try {
                withContext(Dispatchers.IO) {
                    val buf = ByteArray(8192)
                    var n: Int
                    while (stream.read(buf).also { n = it } != -1) {
                        line.write(buf, 0, n)
                    }
                    // Bluetooth A2DP の伝送バッファを押し流すために無音を末尾に追記する。
                    // drain() は Java Sound バッファの終端しか検知できず、A2DP スタックの
                    // 伝送バッファ分（最大 ~200ms）は検知できないため、その分の無音を書く。
                    val silenceFrames = (format.frameRate * BLUETOOTH_TAIL_SILENCE_SEC).toInt()
                    val silence = ByteArray(silenceFrames * format.frameSize)
                    line.write(silence, 0, silence.size)
                    line.drain()
                }
            } finally {
                line.stop()
                line.close()
                currentLine = null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }

    private fun applyVolume(line: SourceDataLine, volume: Int) {
        if (!line.isControlSupported(FloatControl.Type.MASTER_GAIN)) return
        val gainControl = line.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
        val gainDb = if (volume <= 0) {
            gainControl.minimum
        } else {
            (20.0 * kotlin.math.log10(volume / 100.0)).toFloat()
                .coerceIn(gainControl.minimum, gainControl.maximum)
        }
        gainControl.value = gainDb
    }

    companion object {
        private const val BLUETOOTH_TAIL_SILENCE_SEC = 0.3f
    }
}
