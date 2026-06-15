package kurou.kodriver.feature.lmunarrator

import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.FloatControl
import javax.sound.sampled.LineEvent
import kotlin.coroutines.resume
import kotlin.math.log10

class JvmSoundPlayer : SoundPlayer {
    private var currentClip: javax.sound.sampled.Clip? = null

    override val isPlaying: Boolean
        get() = currentClip?.isRunning == true

    override suspend fun play(bytes: ByteArray, volume: Int) = suspendCancellableCoroutine { cont ->
        try {
            val stream = AudioSystem.getAudioInputStream(ByteArrayInputStream(bytes))
            val stereoStream = toStereoIfMono(stream)
            val clip = AudioSystem.getClip()
            clip.open(stereoStream)
            applyVolume(clip, volume)
            clip.addLineListener { event ->
                if (event.type == LineEvent.Type.STOP) {
                    clip.close()
                    currentClip = null
                    if (cont.isActive) cont.resume(Unit)
                }
            }
            currentClip = clip
            clip.start()
            cont.invokeOnCancellation { clip.stop() }
        } catch (_: Exception) {
            cont.resume(Unit)
        }
    }

    private fun toStereoIfMono(stream: AudioInputStream): AudioInputStream {
        val format = stream.format
        if (format.channels != 1) return stream

        val stereoFormat = AudioFormat(
            format.encoding,
            format.sampleRate,
            format.sampleSizeInBits,
            2,
            format.frameSize * 2,
            format.frameRate,
            format.isBigEndian,
        )

        val bytesPerSample = format.sampleSizeInBits / 8
        val monoBytes = stream.readBytes()
        val stereoOut = ByteArrayOutputStream(monoBytes.size * 2)
        var i = 0
        while (i + bytesPerSample <= monoBytes.size) {
            stereoOut.write(monoBytes, i, bytesPerSample)
            stereoOut.write(monoBytes, i, bytesPerSample)
            i += bytesPerSample
        }

        val stereoBytes = stereoOut.toByteArray()
        return AudioInputStream(
            ByteArrayInputStream(stereoBytes),
            stereoFormat,
            stereoBytes.size.toLong() / stereoFormat.frameSize,
        )
    }

    private fun applyVolume(clip: javax.sound.sampled.Clip, volume: Int) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) return
        val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
        val gainDb = if (volume <= 0) {
            gainControl.minimum
        } else {
            (20.0 * log10(volume / 100.0)).toFloat().coerceIn(gainControl.minimum, gainControl.maximum)
        }
        gainControl.value = gainDb
    }
}
