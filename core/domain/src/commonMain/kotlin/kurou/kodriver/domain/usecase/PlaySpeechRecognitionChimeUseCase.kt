package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.SpeechRecognitionChimePlayer

class PlaySpeechRecognitionChimeUseCase(
    private val player: SpeechRecognitionChimePlayer,
) {
    suspend operator fun invoke() = player.play()
}
