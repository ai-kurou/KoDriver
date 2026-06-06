package kurou.kodriver.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import kurou.kodriver.domain.engine.TextToSpeechEngine
import java.util.Locale

internal class AndroidTtsEngine(context: Context) : TextToSpeechEngine {
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.JAPANESE
            } else {
                tts = null
            }
        }
    }

    override fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun stop() {
        tts?.stop()
    }
}
