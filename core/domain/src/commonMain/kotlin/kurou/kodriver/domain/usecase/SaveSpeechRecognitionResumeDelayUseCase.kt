package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.SPEECH_RECOGNITION_RESUME_DELAY_MS_MAX
import kurou.kodriver.domain.model.SPEECH_RECOGNITION_RESUME_DELAY_MS_MIN
import kurou.kodriver.domain.repository.SpeechRecognitionResumeDelayPreferencesRepository

class SaveSpeechRecognitionResumeDelayUseCase(
    private val repository: SpeechRecognitionResumeDelayPreferencesRepository,
) {
    suspend operator fun invoke(delayMs: Int) {
        require(delayMs in SPEECH_RECOGNITION_RESUME_DELAY_MS_MIN..SPEECH_RECOGNITION_RESUME_DELAY_MS_MAX) {
            "delayMs must be between 0 and 5000"
        }
        repository.saveResumeDelayMs(delayMs)
    }
}
