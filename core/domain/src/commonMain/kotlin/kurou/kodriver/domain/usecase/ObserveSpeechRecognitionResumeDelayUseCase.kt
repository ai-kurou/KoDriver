package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.SpeechRecognitionResumeDelayPreferencesRepository

class ObserveSpeechRecognitionResumeDelayUseCase(
    private val repository: SpeechRecognitionResumeDelayPreferencesRepository,
) {
    operator fun invoke() = repository.resumeDelayMs()
}
