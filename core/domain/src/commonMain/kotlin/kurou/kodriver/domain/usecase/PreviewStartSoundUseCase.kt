package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutStartSoundType

public class PreviewStartSoundUseCase(
    private val ttsEngine: TextToSpeechEngine,
) {
    public operator fun invoke(type: ReadoutStartSoundType): Unit = ttsEngine.previewStartSound(type)
}
