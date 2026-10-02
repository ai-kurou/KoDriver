package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.VoicePreferencesRepository

class ObserveVoiceUseCase(
    private val repository: VoicePreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.voiceId()
}
