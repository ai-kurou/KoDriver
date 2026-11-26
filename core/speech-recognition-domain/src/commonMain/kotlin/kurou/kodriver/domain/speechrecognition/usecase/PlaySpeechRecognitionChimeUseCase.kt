package kurou.kodriver.domain.speechrecognition.usecase

import kurou.kodriver.domain.speechrecognition.engine.SpeechRecognitionChimePlayer
import kurou.kodriver.domain.speechrecognition.model.SpeechRecognitionChime

public class PlaySpeechRecognitionChimeUseCase(
    private val player: SpeechRecognitionChimePlayer,
) {
    public suspend operator fun invoke(chime: SpeechRecognitionChime): Unit = player.play(chime)
}
