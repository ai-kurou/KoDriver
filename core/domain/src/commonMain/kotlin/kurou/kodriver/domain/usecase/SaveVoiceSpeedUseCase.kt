package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.VOICE_SPEED_MAX
import kurou.kodriver.domain.model.VOICE_SPEED_MIN
import kurou.kodriver.domain.repository.VoiceSpeedPreferencesRepository

class SaveVoiceSpeedUseCase(
    private val repository: VoiceSpeedPreferencesRepository,
) {
    suspend operator fun invoke(voiceSpeed: Float) {
        require(voiceSpeed in VOICE_SPEED_MIN..VOICE_SPEED_MAX) { "voiceSpeed must be between 0.5 and 2.0" }
        repository.saveVoiceSpeed(voiceSpeed)
    }
}
