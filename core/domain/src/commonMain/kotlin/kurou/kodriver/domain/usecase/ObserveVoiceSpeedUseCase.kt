package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.VoiceSpeedPreferencesRepository

class ObserveVoiceSpeedUseCase(
    private val repository: VoiceSpeedPreferencesRepository,
) {
    operator fun invoke(): Flow<Float> = repository.voiceSpeed()
}
