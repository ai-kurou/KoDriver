package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine

class PlaySpeechEventUseCase(private val ttsEngine: TextToSpeechEngine) {
    operator fun invoke(event: SpeechEvent) = ttsEngine.speak(event)
}
