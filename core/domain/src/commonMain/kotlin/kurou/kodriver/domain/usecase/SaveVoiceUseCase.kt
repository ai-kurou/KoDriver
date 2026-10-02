package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.VoicePreferencesRepository

class SaveVoiceUseCase(
    private val repository: VoicePreferencesRepository,
) {
    suspend operator fun invoke(voiceId: String) {
        repository.saveVoiceId(voiceId)
    }
}
