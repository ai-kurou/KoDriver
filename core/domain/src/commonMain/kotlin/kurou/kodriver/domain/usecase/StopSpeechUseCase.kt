package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.TextToSpeechEngine

class StopSpeechUseCase(
    private val ttsEngine: TextToSpeechEngine,
) {
    operator fun invoke() = ttsEngine.stop()
}
