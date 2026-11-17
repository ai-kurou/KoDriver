package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.LowVolumeWarningEnabledPreferencesRepository

public class SaveLowVolumeWarningEnabledUseCase(
    private val repository: LowVolumeWarningEnabledPreferencesRepository,
) {
    public suspend operator fun invoke(enabled: Boolean) {
        repository.saveLowVolumeWarningEnabled(enabled)
    }
}
