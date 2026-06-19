package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutStartSoundType

class PreviewStartSoundUseCase(private val ttsEngine: TextToSpeechEngine) {
    operator fun invoke(type: ReadoutStartSoundType) = ttsEngine.previewStartSound(type)
}
