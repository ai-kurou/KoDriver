package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.VoicePreferencesRepository

public class ObserveVoiceUseCase(
    private val repository: VoicePreferencesRepository,
) {
    public operator fun invoke(): Flow<String> = repository.voiceId()
}
