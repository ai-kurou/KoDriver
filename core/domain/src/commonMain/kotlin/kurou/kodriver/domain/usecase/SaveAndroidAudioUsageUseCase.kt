package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.AndroidAudioUsage
import kurou.kodriver.domain.repository.AndroidAudioUsagePreferencesRepository

class SaveAndroidAudioUsageUseCase(
    private val repository: AndroidAudioUsagePreferencesRepository,
) {
    suspend operator fun invoke(androidAudioUsage: AndroidAudioUsage) =
        repository.saveAndroidAudioUsage(androidAudioUsage)
}
