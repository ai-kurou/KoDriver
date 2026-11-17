package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.SpeechSettingsSenderRepository

public class OpenWindowsSpeechSettingsUseCase(
    private val repository: SpeechSettingsSenderRepository,
) {
    public operator fun invoke(): Unit = repository.openWindowsSpeechSettings()
}
