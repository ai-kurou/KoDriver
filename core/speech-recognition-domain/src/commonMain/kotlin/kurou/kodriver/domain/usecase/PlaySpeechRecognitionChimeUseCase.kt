package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.SpeechRecognitionChimePlayer
import kurou.kodriver.domain.model.SpeechRecognitionChime

public class PlaySpeechRecognitionChimeUseCase(
    private val player: SpeechRecognitionChimePlayer,
) {
    public suspend operator fun invoke(chime: SpeechRecognitionChime): Unit = player.play(chime)
}
