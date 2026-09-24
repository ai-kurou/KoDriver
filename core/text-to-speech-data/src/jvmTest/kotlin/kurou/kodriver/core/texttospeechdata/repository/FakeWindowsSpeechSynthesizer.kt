package kurou.kodriver.core.texttospeechdata.repository

import kurou.kodriver.core.texttospeechdata.windows.WindowsSpeechSynthesizer

class FakeWindowsSpeechSynthesizer(
    private val available: Boolean = true,
) : WindowsSpeechSynthesizer {
    val spokenTexts = mutableListOf<Pair<String, Boolean>>()
    var stopCount = 0
        private set

    override fun isAvailable(): Boolean = available

    override fun speak(
        text: String,
        queue: Boolean,
    ) {
        spokenTexts += text to queue
    }

    override fun stop() {
        stopCount++
    }
}
