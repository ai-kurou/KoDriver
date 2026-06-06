package kurou.kodriver.tts

import kurou.kodriver.domain.engine.TextToSpeechEngine
import java.io.File
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.LineEvent

internal class WavTtsEngine(
    private val audioDirectory: File = File(System.getProperty("user.home"), ".kodriver/audio"),
) : TextToSpeechEngine {

    private val textToFilename = mapOf(
        "カーレフト" to "car_left.wav",
        "カーライト" to "car_right.wav",
    )

    override fun speak(text: String) {
        val filename = textToFilename[text] ?: return
        val file = audioDirectory.resolve(filename)
        if (!file.exists()) return

        try {
            val clip = AudioSystem.getClip()
            AudioSystem.getAudioInputStream(file).use { stream ->
                clip.open(stream)
            }
            clip.addLineListener { event ->
                if (event.type == LineEvent.Type.STOP) {
                    clip.close()
                }
            }
            clip.start()
        } catch (_: Exception) {
        }
    }

    override fun stop() = Unit
}
