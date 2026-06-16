package kurou.kodriver.feature.lmunarrator

import io.sentry.Sentry
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayInputStream
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
            val clip = AudioSystem.getClip()
            clip.open(stream)
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
        } catch (e: Exception) {
            Sentry.captureException(e)
            cont.resume(Unit)
        }
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
