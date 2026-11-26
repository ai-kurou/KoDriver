package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.VoicePreferencesRepository

public class SaveVoiceUseCase(
    private val repository: VoicePreferencesRepository,
) {
    public suspend operator fun invoke(voiceId: String) {
        repository.saveVoiceId(voiceId)
    }
}
