package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine

public class PlaySpeechEventUseCase(
    private val ttsEngine: TextToSpeechEngine,
) {
    public operator fun invoke(
        event: SpeechEvent,
        queue: Boolean = false,
    ): Unit = ttsEngine.speak(event, queue)
}
