package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.AndroidAudioUsage
import kurou.kodriver.domain.repository.AndroidAudioUsagePreferencesRepository

class ObserveAndroidAudioUsageUseCase(
    private val repository: AndroidAudioUsagePreferencesRepository,
) {
    operator fun invoke(): Flow<AndroidAudioUsage> = repository.observeAndroidAudioUsage()
}
