package kurou.kodriver.feature.narrator

import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.LineEvent
import kotlin.coroutines.resume

class JvmSoundPlayer : SoundPlayer {
    private var currentClip: javax.sound.sampled.Clip? = null

    override val isPlaying: Boolean
        get() = currentClip?.isRunning == true

    override suspend fun play(bytes: ByteArray) = suspendCancellableCoroutine { cont ->
        try {
            val stream = AudioSystem.getAudioInputStream(ByteArrayInputStream(bytes))
            val clip = AudioSystem.getClip()
            clip.open(stream)
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
}
