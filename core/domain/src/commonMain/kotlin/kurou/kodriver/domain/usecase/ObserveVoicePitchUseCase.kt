package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.VoicePitchPreferencesRepository

class ObserveVoicePitchUseCase(
    private val repository: VoicePitchPreferencesRepository,
) {
    operator fun invoke(): Flow<Float> = repository.voicePitch()
}
