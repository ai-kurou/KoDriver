package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LowVolumeWarningEnabledPreferencesRepository

public class ObserveLowVolumeWarningEnabledUseCase(
    private val repository: LowVolumeWarningEnabledPreferencesRepository,
) {
    public operator fun invoke(): Flow<Boolean> = repository.observeLowVolumeWarningEnabled()
}
