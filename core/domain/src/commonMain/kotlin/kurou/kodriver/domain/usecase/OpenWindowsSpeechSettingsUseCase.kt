package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.SpeechSettingsRepository

class OpenWindowsSpeechSettingsUseCase(
    private val repository: SpeechSettingsRepository,
) {
    operator fun invoke() = repository.openWindowsSpeechSettings()
}
