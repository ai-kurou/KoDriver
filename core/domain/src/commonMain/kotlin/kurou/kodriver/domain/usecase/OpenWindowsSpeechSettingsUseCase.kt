package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.SpeechSettingsSenderRepository

class OpenWindowsSpeechSettingsUseCase(
    private val repository: SpeechSettingsSenderRepository,
) {
    operator fun invoke() = repository.openWindowsSpeechSettings()
}
