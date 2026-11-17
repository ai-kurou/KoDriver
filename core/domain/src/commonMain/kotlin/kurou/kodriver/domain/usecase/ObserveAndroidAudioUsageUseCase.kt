package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.AndroidAudioUsage
import kurou.kodriver.domain.repository.AndroidAudioUsagePreferencesRepository

public class ObserveAndroidAudioUsageUseCase(
    private val repository: AndroidAudioUsagePreferencesRepository,
) {
    public operator fun invoke(): Flow<AndroidAudioUsage> = repository.observeAndroidAudioUsage()
}
