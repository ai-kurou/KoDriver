package kurou.kodriver.feature.announcer

import java.io.ByteArrayInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.LineEvent

class JvmSoundPlayer : SoundPlayer {
    private var currentClip: javax.sound.sampled.Clip? = null

    override val isPlaying: Boolean
        get() = currentClip?.isRunning == true

    override fun play(bytes: ByteArray) {
        try {
            val stream = AudioSystem.getAudioInputStream(ByteArrayInputStream(bytes))
            val clip = AudioSystem.getClip()
            clip.open(stream)
            clip.addLineListener { event ->
                if (event.type == LineEvent.Type.STOP) {
                    clip.close()
                    currentClip = null
                }
            }
            currentClip = clip
            clip.start()
        } catch (_: Exception) {
        }
    }
}
