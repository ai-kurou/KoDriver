package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.VoiceSpeedPreferencesRepository

public class ObserveVoiceSpeedUseCase(
    private val repository: VoiceSpeedPreferencesRepository,
) {
    public operator fun invoke(): Flow<Float> = repository.voiceSpeed()
}
