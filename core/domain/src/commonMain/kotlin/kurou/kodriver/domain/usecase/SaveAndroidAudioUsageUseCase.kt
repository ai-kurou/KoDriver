package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.AndroidAudioUsage
import kurou.kodriver.domain.repository.AndroidAudioUsagePreferencesRepository

public class SaveAndroidAudioUsageUseCase(
    private val repository: AndroidAudioUsagePreferencesRepository,
) {
    public suspend operator fun invoke(androidAudioUsage: AndroidAudioUsage): Unit =
        repository.saveAndroidAudioUsage(androidAudioUsage)
}
