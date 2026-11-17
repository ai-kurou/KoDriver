package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.VoicePitchPreferencesRepository

public class ObserveVoicePitchUseCase(
    private val repository: VoicePitchPreferencesRepository,
) {
    public operator fun invoke(): Flow<Float> = repository.voicePitch()
}
