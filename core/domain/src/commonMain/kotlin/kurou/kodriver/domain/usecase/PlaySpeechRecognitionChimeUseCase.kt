package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.SpeechRecognitionChimePlayer
import kurou.kodriver.domain.model.SpeechRecognitionChime

class PlaySpeechRecognitionChimeUseCase(
    private val player: SpeechRecognitionChimePlayer,
) {
    suspend operator fun invoke(chime: SpeechRecognitionChime) = player.play(chime)
}
