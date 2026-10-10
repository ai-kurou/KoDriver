package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.VOICE_PITCH_MAX
import kurou.kodriver.domain.model.VOICE_PITCH_MIN
import kurou.kodriver.domain.repository.VoicePitchPreferencesRepository

class SaveVoicePitchUseCase(
    private val repository: VoicePitchPreferencesRepository,
) {
    suspend operator fun invoke(voicePitch: Float) {
        require(voicePitch in VOICE_PITCH_MIN..VOICE_PITCH_MAX) { "voicePitch must be between 0.5 and 2.0" }
        repository.saveVoicePitch(voicePitch)
    }
}
