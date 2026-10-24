package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LowVolumeWarningEnabledPreferencesRepository

class ObserveLowVolumeWarningEnabledUseCase(
    private val repository: LowVolumeWarningEnabledPreferencesRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeLowVolumeWarningEnabled()
}
