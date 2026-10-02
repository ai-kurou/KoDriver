package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.TTS_CULTURE_NAME
import kurou.kodriver.domain.model.TextToSpeechVoice
import kurou.kodriver.domain.repository.VoiceListRepository

class GetAvailableVoicesUseCase(
    private val repository: VoiceListRepository,
) {
    suspend operator fun invoke(): List<TextToSpeechVoice> =
        repository.availableVoices().filter { it.cultureName == TTS_CULTURE_NAME }.sortedBy { it.displayName }
}
