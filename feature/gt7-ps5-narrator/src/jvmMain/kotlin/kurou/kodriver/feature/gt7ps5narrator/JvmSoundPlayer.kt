package kurou.kodriver.feature.gt7ps5narrator

import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.FloatControl
import javax.sound.sampled.LineEvent
import kotlin.coroutines.resume
import kotlin.math.log10

/**
 * WAV 音声を再生する Jvm 向け実装。
 */
class JvmSoundPlayer : SoundPlayer {
    private var currentClip: javax.sound.sampled.Clip? = null

    override val isPlaying: Boolean
        get() = currentClip?.isRunning == true

    override suspend fun play(
        bytes: ByteArray,
        volume: Int,
    ) = suspendCancellableCoroutine { cont ->
        try {
            val clip = AudioSystem.getClip()
            AudioSystem.getAudioInputStream(ByteArrayInputStream(bytes)).use { stream ->
                clip.open(stream)
            }
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
            captureNarratorError(e)
            cont.resume(Unit)
        }
    }

    private fun applyVolume(
        clip: javax.sound.sampled.Clip,
        volume: Int,
    ) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) return
        val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
        val gainDb =
            if (volume <= 0) {
                gainControl.minimum
            } else {
                (20.0 * log10(volume / 100.0)).toFloat().coerceIn(gainControl.minimum, gainControl.maximum)
            }
        gainControl.value = gainDb
    }
}
