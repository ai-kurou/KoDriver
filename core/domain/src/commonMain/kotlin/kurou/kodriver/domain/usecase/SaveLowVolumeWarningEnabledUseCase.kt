package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.LowVolumeWarningEnabledPreferencesRepository

class SaveLowVolumeWarningEnabledUseCase(
    private val repository: LowVolumeWarningEnabledPreferencesRepository,
) {
    suspend operator fun invoke(enabled: Boolean) {
        repository.saveLowVolumeWarningEnabled(enabled)
    }
}
