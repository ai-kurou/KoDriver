package kurou.kodriver.feature.announcer

import java.io.ByteArrayInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.LineEvent

internal class JvmSoundPlayer : SoundPlayer {
    override fun play(bytes: ByteArray) {
        try {
            val stream = AudioSystem.getAudioInputStream(ByteArrayInputStream(bytes))
            val clip = AudioSystem.getClip()
            clip.open(stream)
            clip.addLineListener { event ->
                if (event.type == LineEvent.Type.STOP) clip.close()
            }
            clip.start()
        } catch (_: Exception) {
        }
    }
}
