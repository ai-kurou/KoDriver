package kurou.kodriver.domain.usecase

import kurou.kodriver.core.model.ReadoutStartSoundType
import kurou.kodriver.domain.engine.TextToSpeechEngine

class PreviewStartSoundUseCase(
    private val ttsEngine: TextToSpeechEngine,
) {
    operator fun invoke(type: ReadoutStartSoundType) = ttsEngine.previewStartSound(type)
}
